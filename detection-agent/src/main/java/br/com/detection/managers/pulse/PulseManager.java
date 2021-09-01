package br.com.detection.managers.pulse;

import java.io.Serializable;

import javax.ejb.Local;

@Local
public interface PulseManager extends Serializable {
	
	void registerAsMember();

}
