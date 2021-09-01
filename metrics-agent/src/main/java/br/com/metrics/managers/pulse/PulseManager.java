package br.com.metrics.managers.pulse;

import java.io.Serializable;

import javax.ejb.Local;

@Local
public interface PulseManager extends Serializable {

	void registerAsMember();
	
}
