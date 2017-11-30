package it.unifi.dinfo.gnocchi;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class PositiveDoubleValidator implements IParameterValidator{
	@Override
	public void validate(String name, String value) throws ParameterException {
		double v = Double.parseDouble(value);
		if(v<=0.0)
			throw new ParameterException(name+" must be positive");
	}
}
