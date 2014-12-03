package com.proclos.colibriweb.entity.component;

import javax.persistence.Entity;

import com.proclos.colibriweb.entity.SelectionItem;

@Entity
public class Library extends SelectionItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4912449795603717348L;
	
	private String fileName;
	private byte[] data;
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public byte[] getData() {
		return data;
	}

}
