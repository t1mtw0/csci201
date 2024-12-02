import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeatherInfo {
    public List<WeatherLocation> data = new ArrayList<WeatherLocation>();

    public WeatherInfo() {
    }

    public void sortAlph() {
        Collections.sort(data);
    }

    public void reverseSort() {
        Collections.sort(data, Collections.reverseOrder());
    }
}
