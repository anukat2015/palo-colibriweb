package at.adaptive.components.session;

import java.util.ArrayList;
import java.util.List;

public abstract class PaginationResultController<T> extends ResultController<T> implements IPaginationResultController<T>
{
	private static final int DEFAULT_MAX_PAGELIST_LENGTH = 10;

	private static final long serialVersionUID = -6308875986225355773L;

	public int getCurrentIndex()
	{
		int index = getFirstResult() + getMaxResults();
		if(index > getResultCount())
		{
			index = getResultCount();
		}
		return index;
	}

	@Override
	public String remove()
	{
		handleLastElementRemoved();
		String res = super.remove();
		return res;
	}

	public Integer getPageResultCount()
	{
		if(results == null)
		{
			return null;
		}
		return results.size();
	}

	@Override
	public void delete(Object id)
	{
		handleLastElementRemoved();
		super.delete(id);
	}

	/**
	 * Sets the first result if a page switch occured after a delete event
	 */
	protected void handleLastElementRemoved()
	{
		// handle last element from results removed
		if(results != null && results.size() == 1 && getCurrentPage() != null && getCurrentPage() > 1)
		{
			setFirstResult((getCurrentPage() - 2) * getMaxResults());
		}
	}

	public Integer getCurrentPage()
	{
		return (getFirstResult() + getMaxResults()) / getMaxResults();
	}

	public List<Integer> getDisplayPages()
	{
		return getCustomDisplayPages(getMaxPageListLength());
	}

	public List<Integer> getCustomDisplayPages(int maxPageListLength)
	{
		List<Integer> displayPages = new ArrayList<Integer>();
		int pageCount = getPageCount();
		int currentPage = getCurrentPage();
		int startIndex = currentPage - maxPageListLength / 2;
		int endIndex = currentPage + maxPageListLength / 2;
		if(startIndex < 1)
		{
			endIndex = endIndex + (startIndex * -1);
			startIndex = 1;
		}
		if(endIndex > pageCount)
		{
			endIndex = pageCount;
			startIndex = endIndex - maxPageListLength;
			if(startIndex < 1)
			{
				startIndex = 1;
			}
		}
		for(int i = startIndex; i < (endIndex - 1); i++)
		{
			displayPages.add(i + 1);
		}
		return displayPages;
	}

	public void getFirstPageResult()
	{
		if(getFirstResult() != null && getFirstResult() == 0)
		{
			getResults();
		}
		else
		{
			setFirstResult(0);
			queryResults();
		}
	}

	public void getLastFirstResult()
	{
		if(getResultCount() != null)
		{
			int pageCount = getPageCount() - 1;
			int maxResults = getMaxResults();
			setFirstResult(pageCount * maxResults);
		}
		else
		{
			setFirstResult(0);
		}
		queryResults();
	}

	public void getNextFirstResult()
	{
		setFirstResult(getFirstResult() + getMaxResults());
		queryResults();
	}

	public Integer getPageCount()
	{
		if(getMaxResults() == null || getResultCount() == null)
		{
			return null;
		}
		else
		{
			int rc = getResultCount().intValue();
			int mr = getMaxResults().intValue();
			int pages = rc / mr;
			return rc % mr == 0 ? pages : pages + 1;
		}
	}

	public void getPageResults(Integer page)
	{
		if(page > getPageCount())
		{
			page = getPageCount();
		}
		setFirstResult((page - 1) * getMaxResults());
		queryResults();
	}

	public void getPreviousFirstResult()
	{
		setFirstResult(firstResult - getMaxResults());
		queryResults();
	}

	public boolean isNextExists()
	{
		if(getResultCount() != null)
		{
			return getFirstResult() + getMaxResults() < getResultCount();
		}
		else
		{
			return false;
		}
	}

	public boolean isPreviousExists()
	{
		return getFirstResult() != null && getFirstResult() != 0;
	}

	/**
	 * Returns the maximum length of the page list to be displayed and created by {@link #getDisplayPages()}
	 * <p>
	 * Subclasses my override
	 * 
	 * @return the maximum length of the page list
	 */
	protected int getMaxPageListLength()
	{
		return DEFAULT_MAX_PAGELIST_LENGTH;
	}
}
