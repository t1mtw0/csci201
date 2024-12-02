public class WeatherLocation implements Comparable<WeatherLocation> {
    public String location;
    public int temperature;
    public int temperatureMin;
    public int temperatureMax;
    public int humidity;
    public float pressureSeaLevel;
    public float visibility;
    public float windSpeed;
    public int windDirection;
    public String condition;

    public WeatherLocation() {
    }

    @Override
    public int compareTo(WeatherLocation otherWL) {
        return location.compareTo(otherWL.location);
    }
}
