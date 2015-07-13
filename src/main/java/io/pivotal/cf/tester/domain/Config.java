package io.pivotal.cf.tester.domain;

import java.util.Date;

public class Config {

	private String ticker;
	private Date time;
	private int open;
	private int low;
	private int high;
	private int close;
	
	public Config() {
	}
	
	public Config(String ticker, Date time, int open, int low, int high, int close) {
		super();
		this.ticker = ticker;
		this.time = time;
		this.open = open;
		this.low = low;
		this.high = high;
		this.close = close;
	}
	
	public String getTicker() {
		return ticker;
	}
	public Date getTime() {
		return time;
	}
	public int getOpen() {
		return open;
	}
	public int getLow() {
		return low;
	}
	public int getHigh() {
		return high;
	}
	public int getClose() {
		return close;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void setOpen(int open) {
		this.open = open;
	}

	public void setLow(int low) {
		this.low = low;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	public void setClose(int close) {
		this.close = close;
	}
	
}
