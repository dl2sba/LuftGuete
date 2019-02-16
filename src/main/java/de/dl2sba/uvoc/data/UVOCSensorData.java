package de.dl2sba.uvoc.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * http://www.jsonschema2pojo.org/
 * 
 * @param iaqAccuracy
 */
public class UVOCSensorData {

	@SerializedName("temperature")
	@Expose
	private Double temperature;
	@SerializedName("pressure")
	@Expose
	private Double pressure;
	@SerializedName("humidity")
	@Expose
	private Double humidity;
	@SerializedName("gasResistance")
	@Expose
	private Integer gasResistance;
	@SerializedName("IAQ")
	@Expose
	private Double iAQ;
	@SerializedName("iaqAccuracy")
	@Expose
	private Integer iaqAccuracy;

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public Double getPressure() {
		return pressure;
	}

	public void setPressure(Double pressure) {
		this.pressure = pressure;
	}

	public Double getHumidity() {
		return humidity;
	}

	public void setHumidity(Double humidity) {
		this.humidity = humidity;
	}

	public Integer getGasResistance() {
		return gasResistance;
	}

	public void setGasResistance(Integer gasResistance) {
		this.gasResistance = gasResistance;
	}

	public Double getIAQ() {
		return iAQ;
	}

	public void setIAQ(Double iAQ) {
		this.iAQ = iAQ;
	}

	public Integer getIaqAccuracy() {
		return iaqAccuracy;
	}

	public void setIaqAccuracy(Integer iaqAccuracy) {
		this.iaqAccuracy = iaqAccuracy;
	}

}