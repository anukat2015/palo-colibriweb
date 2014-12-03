package com.proclos.colibriweb.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//MAP INTERFACE needed for primefaces table sorting - el resolution of dynamic properties
public class DataRow implements Map<String,String> {
	
	private Long id;
	private String[] data;
	private List<String> header;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String[] getData() {
		return data;
	}

	public void setData(String[] data) {
		this.data = data;
	}

	public List<String> getHeader() {
		return header;
	}

	public void setHeader(List<String> header) {
		this.header = header;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean containsKey(Object key) {
		return header.contains(key);
	}

	@Override
	public boolean containsValue(Object arg0) {
		for (String d : data) if (d.equals(arg0)) return true;
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get(Object property) {
		return property != null ? data[header.indexOf(property)] : null;
	}

	@Override
	public boolean isEmpty() {
		return data.length == 0;
	}

	@Override
	public Set<String> keySet() {
		return new HashSet<String>(header);
	}

	@Override
	public String put(String arg0, String arg1) {
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public String remove(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return data.length;
	}

	@Override
	public Collection<String> values() {
		return Arrays.asList(data);
	}
}
