package at.adaptive.components.datahandling.common;

import java.io.Serializable;

public interface Converter<I, O> extends Serializable
{
	O convert(I value);
}
