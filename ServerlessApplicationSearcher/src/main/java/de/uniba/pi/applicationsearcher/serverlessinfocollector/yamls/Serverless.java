package de.uniba.pi.applicationsearcher.serverlessinfocollector.yamls;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Serverless {
	private String service;
	private Provider provider;
	private String frameworkVersion;
	private List<String> plugins;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("service: ").append(this.getService()).append(System.lineSeparator());
		builder.append("\truntime: ").append(this.provider.getRuntime()).append(System.lineSeparator());
		builder.append(System.lineSeparator());
		return builder.toString();
	}


}
