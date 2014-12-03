package com.proclos.colibriweb.session.system;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Duration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.AsynchronousInvocation;
import org.jboss.seam.async.QuartzDispatcher;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.Renderer;
import org.jdom.Element;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.config.ConfigManager;
import com.jedox.etl.core.config.Settings;
import com.jedox.etl.core.execution.ExecutionState;
import com.jedox.etl.core.execution.Executor;
import com.jedox.etl.core.execution.ResultCodes;
import com.jedox.etl.core.logging.ILogListener;
import com.jedox.etl.core.node.IColumn;
import com.jedox.etl.core.node.Row;
import com.jedox.etl.core.source.ISource;
import com.proclos.colibriweb.common.ITaskHolder;
import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.entity.component.ComponentOutput;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.component.Execution;
import com.proclos.colibriweb.entity.component.Library;
import com.proclos.colibriweb.entity.component.Project;
import com.proclos.colibriweb.session.common.PersistenceUtil;

@Name("taskController")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class TaskController {
	
	public enum Codes {
		OK,
		NOFEED
	}
	
	private class JobDescriptor
	{
		private String jobName;
		private String jobGroupName;
		private String description;
		private String type;
		private Trigger trigger;
		private long runtime;
		private Component component;

		public long getRuntime()
		{
			return runtime;
		}

		public void setRuntime(long runtime)
		{
			this.runtime = runtime;
		}

		public void setJobName(String jobName)
		{
			this.jobName = jobName;
		}

		public String getJobName()
		{
			return jobName;
		}

		public void setJobGroupName(String jobGroupName)
		{
			this.jobGroupName = jobGroupName;
		}

		public String getJobGroupName()
		{
			return jobGroupName;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public String getDescription()
		{
			return description;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getType()
		{
			return type;
		}

		public void setTrigger(Trigger trigger)
		{
			this.trigger = trigger;
		}

		public Trigger getTrigger()
		{
			return trigger;
		}

		public Component getComponent() {
			return component;
		}

		public void setComponent(Component component) {
			this.component = component;
		}
	}
	
	private class LogListener extends Thread  implements ILogListener {
		private Long componentId;
		private StringBuffer buffer = new StringBuffer();
		private StringBuffer log = new StringBuffer();
		private boolean active = true;
		private boolean push;
		private boolean recordLogs;
		
		public LogListener(Long componentId, boolean push, boolean recordLogs) {
			this.componentId = componentId;
			this.setPriority(Thread.NORM_PRIORITY);
			this.push = push;
			this.recordLogs = recordLogs;
		}

		@Override
		public void addMessage(String type, Long timestamp, String message) {
			buffer.append(message); //this is to slow down message push if we experience client side problems
		}
		
		public void setActive(boolean active) {
			this.active = active;
			if (active == false && buffer.length() > 0) flush();
		}
		
		public void flush() {
			if (push) {
				EventBus eventBus = EventBusFactory.getDefault().eventBus();
	            eventBus.publish("/log/"+String.valueOf(componentId), buffer.toString());
			}
			if (recordLogs) log.append(buffer.toString());
			buffer.setLength(0);
		}
		
		public String getMessagesText() {
			return log.toString();
		}
		
		public void run() {
			//this is to slow down message push if we experience client side problems
			try {
				while (active) {
					if (buffer.length() > 0) flush();
					sleep(50);
				}
			}
			catch (InterruptedException e) {}
			//*/
		}
		
	}

	
	//@Logger
	//private Log log;
	@In(create = true)
	protected Renderer renderer;
	
	protected static Log log = LogFactory.getLog(TaskController.class);
	protected static DocumentBuilderFactory docBuilderfactory = DocumentBuilderFactory.newInstance();
	
	private String mailMessage;
	private ITaskHolder taskHolder;

	private Boolean cronEnabled = Boolean.TRUE;
	private Boolean mailEnabled = Boolean.TRUE;
	private Boolean expireJobs = Boolean.FALSE;
	private Boolean logExceptionStack = Boolean.FALSE;
	private String taskLogLevel = "INFO";
	private String mailUser = "chris@proclos.com";

	
	@Create
	public void create()  {
		Logger.getLogger(TaskController.class).setLevel(Level.toLevel("taskLogLevel"));
		@SuppressWarnings("unchecked")
		List<Project> projects = (List<Project>)getTaskEntityManager().createQuery("from Project project where project.active = true").getResultList();
		@SuppressWarnings("unchecked")
		List<Library> library = (List<Library>)getTaskEntityManager().createQuery("from Library library where library.active = true").getResultList();
		
		try {
			for (Project p : projects) {
				Locator l = new Locator().add(p.getName());
				Element config = ConfigManager.getInstance().get(l);
				if (config == null || !config.getName().equals("project")) {
					ConfigManager.getInstance().add(l, p.getProjectXML());
				}
			}
			for (Library l : library) {
				Settings.getInstance().getCustomlibDir();
				String path = Settings.getInstance().getCustomlibDir()+"/"+l.getFileName();
				if (!new File(Settings.getInstance().getCustomlibDir()).exists()) {
					new File(Settings.getInstance().getCustomlibDir()).mkdirs();
				}
				FileOutputStream stream = new FileOutputStream(path);
				stream.write(l.getData());
			    stream.flush();
			    stream.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public synchronized void mail(String type, String message, ITaskHolder holder) {
		if (mailEnabled) {
			String viewId = "/elements/"+type+"message.xhtml";
			mailMessage = message;
			taskHolder = holder;
			renderer.render(viewId);
		}
	}
	
	public String getMailMessage() {
		return mailMessage;
	}

	public ITaskHolder getTaskHolder() {
		return taskHolder;
	}
	
	
	public EntityManager getTaskEntityManager() {
		EntityManagerFactory taskEMF;
		taskEMF = (EntityManagerFactory) Expressions.instance().createValueExpression("#{entityManagerFactory}").getValue();
		EntityManager manager = taskEMF.createEntityManager();
		manager.setFlushMode(FlushModeType.COMMIT);
		return manager;
	}
	

	public Long getDurationFromInterval(String interval) {
		try {
			CronExpression expression = new CronExpression(interval);
			Long duration = expression.getNextValidTimeAfter(new Date(0)).getTime(); //calculate duration from cron string for single run.
			return duration;
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle invokeExecution(@IntervalCron String interval, Long componentId, String locatorString)
	{
		EntityManager manager = getTaskEntityManager();
		Long startTime = System.currentTimeMillis();
		Component component = null;
		ExecutionState state = null;
		boolean error = false;
		Locator locator = Locator.parse(locatorString);
		LogListener logs = new LogListener(componentId,false,true);
		try {
			com.jedox.etl.core.execution.Execution etlExecution = Executor.getInstance().createExecution(locator, new Properties());
			if (etlExecution.getExecutable() instanceof ISource) {
				etlExecution.getExecutable().getParameter().setProperty("sample", String.valueOf(200));
			}
			Executor.getInstance().addExecution(etlExecution);
			if (etlExecution != null) {
				etlExecution.getExecutionState().setMessageWriter(logs);
				logs.start();
				Long executionId = etlExecution.getKey();
				Execution execution = new Execution();
				execution.setType(locator.getManager().substring(0,1).toUpperCase()+locator.getManager().substring(1,locator.getManager().length()-1));
				component = manager.find(Component.class, componentId);
				execution.setComponent(component);
				execution.setStartDate(new Date());
				execution.setEtlId(executionId);
				manager.getTransaction().begin();
				manager.persist(execution);
				manager.getTransaction().commit();
				DirtyStateManager dirtyStateManager = (DirtyStateManager)org.jboss.seam.Component.getInstance("dirtyStateManager");
				dirtyStateManager.setDirty("execution");
				manager.getTransaction().begin();
				//run execution
				Executor.getInstance().runExecution(executionId);
				state = Executor.getInstance().getExecutionState(executionId, true);
				logs.setActive(false);
				execution.setStatus(state.getStatus());
				execution.setLog(logs.getMessagesText());
				execution.setFirstErrorMessage(state.getFirstErrorMessage());
				execution.setStopDate(state.getStopDate());
				execution.setErrors(state.getErrors());
				execution.setWarnings(state.getErrors());
				manager.getTransaction().commit();
				/*
				PushContext pushContext = PushContextFactory.getDefault().getPushContext(); 
				pushContext.push("/"+String.valueOf(execution.getId()),etlExecution.getExecutionState().getString(etlExecution.getExecutionState().getStatus()));
				*/
			}
		}
		catch (Exception e) {
			log.error("Execution "+componentId+" failed with exception:", e);
			error = true;
		}
		finally {
			//checkReschedule(manager, feed); //non ui
			if (error || (state != null && (ResultCodes.Codes.statusErrors.equals(state.getStatus()) || ResultCodes.Codes.statusFailed.equals(state.getStatus()) || ResultCodes.Codes.statusInvalid.equals(state.getStatus())))) {
				try {
					if (component != null && component instanceof ITaskHolder) {
						String to = component.getContactOnError();
						if (to != null && !to.isEmpty()) {
							mail("schedulererror",state != null ? logs.getMessagesText() : "Execution failed.",(ITaskHolder)component);
						}
					}
				}
				catch (Exception e1) {
					if (getLogExceptionStack()) {
						log.error("Sending mail (task error) failed:", e1);
					} else {
						log.error("Sending mail (task error) failed:"+ e1.getMessage());
					}
				}
			}
		}
		manager.close();
		return null;
	}
	
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle invokeExecution(@Duration Long interval, Long componentId, Long executionId, boolean persistent, Long pushId)
	{
		EntityManager manager = getTaskEntityManager();
		Long startTime = System.currentTimeMillis();
		String result = "not executed";
		LogListener logs = null;
		try {
			com.jedox.etl.core.execution.Execution etlExecution = Executor.getInstance().getExecution(executionId);
			if (etlExecution != null) {
				Locator locator = etlExecution.getExecutable().getLocator();
				logs = new LogListener(pushId,componentId.equals(pushId),true);
				etlExecution.getExecutionState().setMessageWriter(logs);
				logs.start();
				Execution execution = new Execution();
				Component component = manager.find(Component.class, componentId);
				if (persistent) {
					execution.setType(locator.getManager().substring(0,1).toUpperCase()+locator.getManager().substring(1,locator.getManager().length()-1));
					execution.setComponent(component);
					execution.setStartDate(new Date());
					execution.setEtlId(executionId);
					manager.getTransaction().begin();
					manager.persist(execution);
					manager.getTransaction().commit();
					DirtyStateManager dirtyStateManager = (DirtyStateManager)org.jboss.seam.Component.getInstance("dirtyStateManager");
					dirtyStateManager.setDirty("execution");
				}
				//run execution
				Executor.getInstance().runExecution(executionId);
				ExecutionState state = Executor.getInstance().getExecutionState(executionId, true);
				result = state.getString(state.getStatus());
				boolean persistMetadata = state.getMetadata() != null; //&& !(ConfigManager.getInstance().getComponent(locator, IContext.defaultName) instanceof ITreeSource);
				if (persistent || persistMetadata) {
					manager.getTransaction().begin();
					if (persistent) {
						execution.setStatus(state.getStatus());
						execution.setLog(logs.getMessagesText());
						execution.setFirstErrorMessage(state.getFirstErrorMessage());
						execution.setStopDate(state.getStopDate());
						execution.setErrors(state.getErrors());
						execution.setWarnings(state.getErrors());
						manager.persist(execution);
					}
					if (persistMetadata) updateOutputDescription(state, component, manager);
					manager.getTransaction().commit();
				}
			}
		}
		catch (Exception e) {
			log.error("Execution "+componentId+" failed with exception:", e);
		}
		finally {
			//checkReschedule(manager, feed); //non ui
			long duration = System.currentTimeMillis() - startTime;
			/*
			if (duration < 1000) { //give UI time to rerender
				try {
					Thread.sleep(1000-duration);
				} catch (InterruptedException e) {}
			}
			*/
			if (logs != null) logs.setActive(false);
			EventBus eventBus = EventBusFactory.getDefault().eventBus();
            eventBus.publish("/completed/"+String.valueOf(pushId), result);
		}
		manager.close();
		return null;
	}	
	
	private void updateOutputDescription(ExecutionState state, Component component, EntityManager manager) {
		Row outputDescription = state.getMetadata();
		if (component != null) {
			List<ComponentOutput> outputList = new ArrayList<ComponentOutput>();
			for (IColumn c : outputDescription.getColumns()) {
				ComponentOutput output = new ComponentOutput();
				output.setComponent(component);
				output.setName(c.getName());
				output.setType(c.getValueType().toString());
				outputList.add(output);
			}
			//check persistence
			if (component.getId() != null) {
				@SuppressWarnings("unchecked")
				List<ComponentOutput> toDelete = (List<ComponentOutput>)manager.createQuery("from ComponentOutput where component.id=:id").setParameter("id", component.getId()).getResultList();
				for (ComponentOutput co : toDelete) {
					manager.remove(co);
				}
				manager.flush();
			}
			for (ComponentOutput co : outputList) {
				manager.persist(co);
			}
		}
	}

	public void setCronEnabled(Boolean cronEnabled) {
		this.cronEnabled = cronEnabled;
		try {
			if (!cronEnabled) {
				QuartzDispatcher dispatcher = (QuartzDispatcher) Expressions.instance().createValueExpression("#{org.jboss.seam.async.dispatcher}").getValue();
				dispatcher.getScheduler().pauseAll();
			} else {
				QuartzDispatcher dispatcher = (QuartzDispatcher) Expressions.instance().createValueExpression("#{org.jboss.seam.async.dispatcher}").getValue();
				dispatcher.getScheduler().resumeAll();
			}
		}
		catch (Exception e) {
			log.error("Altering scheduler state failed: "+e.getMessage());
		}
	}

	public Boolean getCronEnabled() {
		return cronEnabled;
	}

	public void setMailEnabled(Boolean mailEnabled) {
		this.mailEnabled = mailEnabled;
	}

	public Boolean getMailEnabled() {
		return mailEnabled;
	}

	public void setTaskLogLevel(String taskLogLevel) {
		this.taskLogLevel = taskLogLevel;
	}

	public String getTaskLogLevel() {
		return taskLogLevel;
	}

	public void setLogExceptionStack(Boolean logExceptionStack) {
		this.logExceptionStack = logExceptionStack;
	}

	public Boolean getLogExceptionStack() {
		return logExceptionStack;
	}

	public void setExpireJobs(Boolean expireJobs) {
		this.expireJobs = expireJobs;
	}

	public Boolean getExpireJobs() {
		return expireJobs;
	}

	public void setMailUser(String mailUser) {
		this.mailUser = mailUser;
	}

	public String getMailUser() {
		return mailUser;
	}
	
	public void killAllJobs()
	{
		for(JobDescriptor d : getJobDescriptors())
		{
			killJob(d);
		}
	}
	
	private String getComponentType(int type) {
		switch (type) {
		case ComponentTypes.EXTRACT : return "Extract";
		case ComponentTypes.TRANSFORM: return "Transform";
		case ComponentTypes.LOAD: return "Load";
		case ComponentTypes.JOB: return "Job";
		default: return "Unknown";
		}
	}

	public List<JobDescriptor> getJobDescriptors()
	{
		EntityManager manager = getTaskEntityManager();
		List<JobDescriptor> result = new ArrayList<JobDescriptor>();
		Map<Trigger, JobExecutionContext> running = new HashMap<Trigger, JobExecutionContext>();
		try
		{
			List<?> list = QuartzDispatcher.instance().getScheduler().getCurrentlyExecutingJobs();
			for(int i = 0; i < list.size(); i++)
			{
				JobExecutionContext c = (JobExecutionContext)list.get(i);
				running.put(c.getTrigger(), c);
			}
			for(String g : QuartzDispatcher.instance().getScheduler().getJobGroupNames())
			{
				for(String j : QuartzDispatcher.instance().getScheduler().getJobNames(g))
				{
					try
					{
						for(Trigger t : QuartzDispatcher.instance().getScheduler().getTriggersOfJob(j, g))
						{
							JobDescriptor d = new JobDescriptor();
							d.setJobGroupName(g);
							d.setJobName(j);
							d.setTrigger(t);
							JobDetail detail = QuartzDispatcher.instance().getScheduler().getJobDetail(j, g);
							JobDataMap m = detail.getJobDataMap();
							AsynchronousInvocation invokation = (AsynchronousInvocation)m.get("async");
							Field methodNameField = invokation.getClass().getDeclaredField("methodName"); // NoSuchFieldException
							methodNameField.setAccessible(true);
							String methodName = (String)methodNameField.get(invokation); // IllegalAccessException
							Field argsField = invokation.getClass().getDeclaredField("args");
							argsField.setAccessible(true);
							Object[] args = (Object[])argsField.get(invokation); // IllegalAccessException
							if(methodName.equals("invokeExecution"))
							{
								Long componentID = (Long)args[1];
								Component c = manager.find(Component.class, componentID);
								if (c != null) {
									d.setDescription(c.getName());
									PersistenceUtil.initializeCollection(c.getExecutions());
									d.setType(getComponentType(c.getdType()));
									d.setComponent(c);
									result.add(d);
								}
							}
							JobExecutionContext c = running.get(t);
							if(c != null) d.setRuntime(c.getJobRunTime());
						}
					}
					catch(Exception e)
					{
						log.warn("Error interupting job " + j + ": " + e.getMessage());
					}
				}
			}
		}
		catch(Exception e)
		{
			log.warn("Error getting job descriptors: " + e.getMessage());
		}
		return result;
	}

	public void killJob(JobDescriptor job)
	{
		try
		{
			for (Execution e : job.getComponent().getExecutions()) {
				if (e.getStopDate() == null && e.getEtlId() != null) {
					try {
						ExecutionState state = Executor.getInstance().stop(e.getEtlId());
						try {
							Thread.sleep(1000);
							state = Executor.getInstance().getExecutionState(e.getEtlId(), false);
							if (state != null && ResultCodes.Codes.statusStopping.equals(state.getStatus())) Executor.getInstance().stop(e.getEtlId());
						}
						catch (Exception ex1) {};
					} catch (Exception ex) {
						log.warn("Failed to stop execution "+e.getEtlId());
					}
				}
			}
			if (job.getComponent() instanceof ITaskHolder) {
				ITaskHolder t = (ITaskHolder)job.getComponent();
				if (Boolean.FALSE.equals(t.getIsManual()) && t.getHandle() != null) {
					t.setIsManual(true);
					t.setHandle(null);
					QuartzDispatcher.instance().getScheduler().unscheduleJob(job.getTrigger().getName(), job.getTrigger().getGroup());
					EntityManager manager = getTaskEntityManager();
					manager.getTransaction().begin();
					manager.persist(t);
					manager.getTransaction().commit();
					manager.close();
				}
			}
		}
		catch(Exception e)
		{
			log.warn("Error while killing " + job.getType() + " " + job.getDescription() + ": " + e.getMessage());
		}
	}
	
	/*
	 * 	private void checkReschedule(EntityManager manager, ITaskHolder taskholder) {
		if (!Boolean.TRUE.equals(expireJobs) && !Boolean.TRUE.equals(taskholder.getIsManual()) && taskholder.getHandle() != null && taskholder.getInterval() != null && getDurationFromInterval(taskholder.getInterval().getName()) != null) {
			try {
				Trigger trigger = taskholder.getHandle().getTrigger();
				SimpleTrigger newTrigger = new SimpleTrigger(trigger.getName(),trigger.getGroup(),new Date(new Date().getTime()+getDurationFromInterval(taskholder.getInterval().getName())));
				newTrigger.setJobName(trigger.getJobName());
				newTrigger.setJobGroup(trigger.getJobGroup());
				QuartzDispatcher dispatcher = (QuartzDispatcher) Expressions.instance().createValueExpression("#{org.jboss.seam.async.dispatcher}").getValue();
				dispatcher.getScheduler().rescheduleJob(trigger.getName(), trigger.getGroup(), newTrigger);
			}
			catch (Exception e) {
				if (getLogExceptionStack()) {
					log.error("Rescheduling Task for "+taskholder.getClass().getSimpleName()+" with id "+taskholder.getId()+" failed with exception:", e);
				} else {
					log.error("Rescheduling Task for "+taskholder.getClass().getSimpleName()+" with id "+taskholder.getId()+" failed with exception:"+ e.getMessage());
				}
			}
		} else {
			manager.getTransaction().begin();
			try {
				taskholder.setIsManual(true);
				taskholder.setHandle(null);
				manager.persist(taskholder);
				manager.flush();
				manager.getTransaction().commit();
			}
			catch (Exception e) {
				manager.getTransaction().rollback();
				if (getLogExceptionStack()) {
					log.error("Resetting "+taskholder.getClass().getSimpleName()+" with id "+taskholder.getId()+"to manual failed with exception:", e);
				} else {
					log.error("Resetting "+taskholder.getClass().getSimpleName()+" with id "+taskholder.getId()+"to manual failed with exception:"+ e.getMessage());
				}
			}
			try {
				String to = taskholder.getContactOnError();
				if (to != null && !to.isEmpty()) {
					StringBuffer buf = new StringBuffer("Klasse: "+taskholder.getClass().getSimpleName());
					mail("taskexpiry",buf.toString(),taskholder);
				}
			}
			catch (Exception e) {
				if (getLogExceptionStack()) {
					log.error("Sending mail (task expiry) failed:", e);
				} else {
					log.error("Sending mail (task expiry) failed:"+ e.getMessage());
				}
			}
		}
	}
	 */

}
