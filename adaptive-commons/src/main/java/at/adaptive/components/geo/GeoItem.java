package at.adaptive.components.geo;

public class GeoItem
{
	private String address;
	private String postalCode;
	private String locality;
	private String latitude;
	private String longitude;

	public GeoItem()
	{
		super();
	}

	public GeoItem(String latitude, String longitude)
	{
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof GeoItem)) return false;
		GeoItem other = (GeoItem)obj;
		if(latitude == null)
		{
			if(other.latitude != null) return false;
		}
		else if(!latitude.equals(other.latitude)) return false;
		if(longitude == null)
		{
			if(other.longitude != null) return false;
		}
		else if(!longitude.equals(other.longitude)) return false;
		return true;
	}

	public String getAddress()
	{
		return address;
	}

	public String getLatitude()
	{
		return latitude;
	}

	public String getLocality()
	{
		return locality;
	}

	public String getLongitude()
	{
		return longitude;
	}

	public String getPostalCode()
	{
		return postalCode;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		return result;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public void setLatitude(String latitude)
	{
		this.latitude = latitude;
	}

	public void setLocality(String locality)
	{
		this.locality = locality;
	}

	public void setLongitude(String longitude)
	{
		this.longitude = longitude;
	}

	public void setPostalCode(String postalCode)
	{
		this.postalCode = postalCode;
	}

	@Override
	public String toString()
	{
		return "GeoItem[lat=" + getLatitude() + "; long=" + getLongitude() + "; address=" + getAddress() + "; postalCode=" + getPostalCode() + "; locality=" + getLocality() + "]";
	}
}
