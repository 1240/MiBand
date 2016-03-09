package ru.l240.miband.miband;


import java.util.Observable;


public class MiBand extends Observable {

	private String mBTAddress;
	private int mSteps;
	private String mName;
	private Battery mBattery;


	public String getmBTAddress() {
		return mBTAddress;

	}

	public void setmBTAddress(String mBTAddress) {
		this.mBTAddress = mBTAddress;
		setChanged();
		notifyObservers();
	}

	public int getmSteps() {
		return mSteps;
	}

	public void setmSteps(int mSteps) {
		this.mSteps = mSteps;
		setChanged();
		notifyObservers();
	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
		setChanged();
		notifyObservers();
	}

	public Battery getmBattery() {
		return mBattery;
	}

	public void setmBattery(Battery mBattery) {
		this.mBattery = mBattery;
		setChanged();
		notifyObservers();
	}
}
