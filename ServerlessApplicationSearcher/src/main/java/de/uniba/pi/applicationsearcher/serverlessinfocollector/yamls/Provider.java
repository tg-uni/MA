package de.uniba.pi.applicationsearcher.serverlessinfocollector.yamls;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Provider {
	private String runtime;
	private String name;
	private String region;
}
