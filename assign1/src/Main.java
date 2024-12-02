import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

class Main {
    static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        Boolean success = false;
        Gson gson = new Gson();
        WeatherInfo weatherInfo = null;
        String fileN = "";

        do {
            System.out.print("What is the name of the weather file? ");
            fileN = in.nextLine();
            FileReader fr = null;
            BufferedReader br = null;
            StringBuilder contents = new StringBuilder();
            try {
                fr = new FileReader(fileN);
                br = new BufferedReader(fr);
                String line = br.readLine();
                while (line != null) {
                    contents.append(line);
                    line = br.readLine();
                }
                weatherInfo = gson.fromJson(contents.toString(), WeatherInfo.class);
                success = true;
            } catch (FileNotFoundException fnf) {
                System.out.println("File " + fileN + " not found.");
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            } catch (JsonParseException jpe) {
                System.out.println("File " + fileN + " is not formatted properly.");
                System.out.println(jpe.getMessage());
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException ioe) {
                        System.out.println(ioe.getMessage());
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ioe) {
                        System.out.println(ioe.getMessage());
                    }
                }
            }
        } while (!success);

        System.out.println();

        Boolean using = true;

        while (using) {
            System.out.println("\t1) Display weather on all locations");
            System.out.println("\t2) Search for weather on a location");
            System.out.println("\t3) Add a new location");
            System.out.println("\t4) Remove a location");
            System.out.println("\t5) Sort locations");
            System.out.println("\t6) Exit");

            System.out.print("\nWhat would you like to do? ");
            int option = 0;
            do {
                if (!in.hasNextInt()) {
                    in.next();
                    continue;
                }
                option = in.nextInt();
                System.out.println();
            } while (option < 1 || option > 6);

            switch (option) {
                case 1:
                    displayWeather(weatherInfo);
                    break;
                case 2:
                    searchWeather(weatherInfo);
                    break;
                case 3:
                    addLocation(weatherInfo);
                    break;
                case 4:
                    removeLocation(weatherInfo);
                    break;
                case 5:
                    sortLocations(weatherInfo);
                    break;
                case 6:
                    quit(weatherInfo, gson, fileN);
                    System.out.println("Thank you for using my program!");
                    using = false;
                    in.close();
                    break;
            }
        }
    }

    private static void displayWeather(WeatherInfo weatherInfo) {
        for (WeatherLocation wi : weatherInfo.data) {
            System.out.println(wi.location);
            System.out.println("\ttemperature is " + wi.temperature + " degrees Fahrenheit,");
            System.out.println("\tlow temperature is " + wi.temperatureMin + " degrees Fahrenheit,");
            System.out.println("\thigh temperature is " + wi.temperatureMax + " degrees Fahrenheit,");
            System.out.println("\thumidity is " + wi.humidity + "%,");
            System.out.println("\tpressure is " + wi.pressureSeaLevel + " Inch Hg,");
            System.out.println("\tvisibility is " + wi.visibility + " miles,");
            System.out.println("\twind speed is " + wi.windSpeed + " miles/hour,");
            System.out.println("\twind direction is " + wi.windDirection + " degreed,");
            System.out.println("\tweather can be described as " + wi.condition);
            System.out.println();
        }
    }

    private static void searchWeather(WeatherInfo weatherInfo) {
        Boolean success = false;
        String location = "";
        WeatherLocation wo = null;
        do {
            System.out.print("What is the location you would like to search for? ");
            location = in.nextLine().toLowerCase();
            for (WeatherLocation wi : weatherInfo.data) {
                if (location.equals(wi.location.toLowerCase())) {
                    success = true;
                    wo = wi;
                    break;
                }
            }
            if (!success) {
                System.out.println(location + " could not be found.");
            }
        } while (!success);

        System.out.println("I have information about the weather in Los Angeles.");
        System.out.println();

        while (true) {
            System.out.println("\t1) temperature");
            System.out.println("\t2) high and low temperature");
            System.out.println("\t3) humidity");
            System.out.println("\t4) pressure");
            System.out.println("\t5) visibility");
            System.out.println("\t6) wind speed and direction");
            System.out.println("\t7) weather conditions");
            System.out.println("\t8) return to main menu");
            System.out.println();

            int option = 0;
            do {
                System.out.print("What weather information do you want to know for " + location + "? ");
                if (!in.hasNextInt()) {
                    in.next();
                    continue;
                }
                option = in.nextInt();
                System.out.println();
            } while (option < 1 || option > 8);
            
            switch (option) {
                case 1:
                    System.out.println(
                            "The temperature in " + location + " is " + wo.temperature + " degrees Fahrenheit.");
                    System.out.println();
                    break;
                case 2:
                    System.out.println("The high/low temperature in " + location + " is " + wo.temperatureMin + "/"
                            + wo.temperatureMax + " degrees Fahrenheit.");
                    System.out.println();
                    break;
                case 3:
                    System.out.println("The humidity in " + location + " is " + wo.humidity + "%.");
                    System.out.println();
                    break;
                case 4:
                    System.out.println("The pressure in " + location + " is " + wo.pressureSeaLevel + " Inch Hg.");
                    System.out.println();
                    break;
                case 5:
                    System.out.println("The visiblity in " + location + " is " + wo.visibility + " miles.");
                    System.out.println();
                    break;
                case 6:
                    System.out.println("The wind speed and direction in " + location + " is " + wo.windSpeed
                            + " miles/hour and " + wo.windDirection + " degreed.");
                    System.out.println();
                    break;
                case 7:
                    System.out.println("The weather in " + location + " can be described as " + wo.condition + ".");
                    System.out.println();
                    break;
                case 8:
                    System.out.println();
                    return;
            }
        }
    }

    private static void addLocation(WeatherInfo weatherInfo) {
        Boolean found = false;
        String location = "";
        do {
            found = false;
            System.out.print("What is the location you would like to add weather info? ");
            location = in.nextLine().toLowerCase();
            for (WeatherLocation wi : weatherInfo.data) {
                if (location.equals(wi.location.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                System.out.println("There is already an entry for " + location + ".");
            }
        } while (found);

        System.out.println();

        Boolean success = false;

        int temp = 0;
        do {
            System.out.print("What is the temperature? ");
            if (!in.hasNextInt()) {
                in.next();
                continue;
            }
            temp = in.nextInt();
            success = true;
            System.out.println();
        } while (!success);

        success = false;

        int lowTemp = 0;
        do {
            System.out.print("What is the low temperature? ");
            while (!in.hasNextInt()) {
                in.next();
                continue;
            }
            lowTemp = in.nextInt();
            success = true;
            System.out.println();
        } while (!success);

        success = false;

        int highTemp = 0;
        do {
            System.out.print("What is the high temperature? ");
            while (!in.hasNextInt()) {
                in.next();
                continue;
            }
            highTemp = in.nextInt();
            success = true;
            System.out.println();
        } while (!success);

        success = false;

        int humidity = 0;
        do {
            System.out.print("What is the humidity? ");
            while (!in.hasNextInt()) {
                in.next();
                continue;
            }
            humidity = in.nextInt();
            success = true;
            System.out.println();
        } while (!success);

        success = false;

        float pressure = 0.0f;
        do {
            System.out.print("What is the pressure? ");
            while (!in.hasNextFloat()) {
                in.next();
                continue;
            }
            pressure = in.nextFloat();
            success = true;
            System.out.println();
        } while (!success);

        success = false;

        float visibility = 0.0f;
        do {
            System.out.print("What is the visibility? ");
            while (!in.hasNextFloat()) {
                in.next();
                continue;
            }
            visibility = in.nextFloat();
            success = true;
            System.out.println();
        } while (!success);

        success = false;

        float windSpeed = 0.0f;
        do {
            System.out.print("What is the wind speed? ");
            while (!in.hasNextFloat()) {
                in.next();
                continue;
            }
            windSpeed = in.nextFloat();
            success = true;
            System.out.println();
        } while (!success);

        success = false;

        int windDirection = 0;
        do {
            System.out.print("What is the wind direction? ");
            while (!in.hasNextInt()) {
                in.next();
                continue;
            }
            windDirection = in.nextInt();
            success = true;
            System.out.println();
        } while (!success);

        success = false;

        String condition;
        do {
            System.out.print("What is the weather condition? ");
            while (!in.hasNextLine()) {
                in.next();
                continue;
            }
            condition = in.nextLine();
            success = true;
            System.out.println();
        } while (!success);
        
        WeatherLocation newWI = new WeatherLocation();
        newWI.location = location;
        newWI.temperature = temp;
        newWI.temperatureMin = lowTemp;
        newWI.temperatureMax = highTemp;
        newWI.humidity = humidity;
        newWI.pressureSeaLevel = pressure;
        newWI.visibility = visibility;
        newWI.windSpeed = windSpeed;
        newWI.windDirection = windDirection;
        newWI.condition = condition;
        weatherInfo.data.add(newWI);
        System.out.println("There is now a new entry for " + location + ".");
        System.out.println();
    }

    private static void removeLocation(WeatherInfo weatherInfo) {
        int i = 0;
        for (i = 0; i < weatherInfo.data.size(); i++) {
            System.out.println("\t" + (i + 1) + ") " + weatherInfo.data.get(i).location);
        }
        System.out.println();
        int locToRemove = 0;
        do {
            System.out.print("Which location would you like to remove? ");
            if (!in.hasNextInt()) {
                in.next();
                continue;
            }
            locToRemove = in.nextInt();
            System.out.println();
        } while (locToRemove < 1 || locToRemove > i + 1);
        
        WeatherLocation location = weatherInfo.data.get(locToRemove - 1);
        weatherInfo.data.remove(location);
        System.out.println(location.location + " is now removed.");
        System.out.println();
    }

    private static void sortLocations(WeatherInfo weatherInfo) {
        System.out.println("\t1) A to Z");
        System.out.println("\t2) Z to A");
        System.out.println();

        int choice = 0;
        do {
            System.out.print("How would you like to sort? ");
            if (!in.hasNextInt()) {
                in.next();
                continue;
            }
            choice = in.nextInt();
            System.out.println();
        } while (choice < 1 || choice > 2);
        if (choice == 1) {
            weatherInfo.sortAlph();
            System.out.println("Your locations are now sorted in alphabetical order (A-Z)");
        } else {
            weatherInfo.reverseSort();
            System.out.println("Your locations are now sorted in reverse alphabetical order (Z-A)");
        }
        System.out.println();
    }

    private static void quit(WeatherInfo weatherInfo, Gson gson, String fileName) {
        System.out.println("\t1) Yes");
        System.out.println("\t2) No");
        System.out.println();

        int choice = 0;
        do {
            System.out.print("Would you like to save your edits? ");
            if (!in.hasNextInt()) {
                in.next();
                continue;
            }
            choice = in.nextInt();
            System.out.println();
        } while (choice < 0 || choice > 2);
        
        if (choice == 2) {
            return;
        }
        System.out.println(choice);

        String contents = gson.toJson(weatherInfo);

        try {
	        FileWriter fw = new FileWriter(fileName);
	        fw.write(contents);
	        fw.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        
        System.out.println("Your edits have been saved to " + fileName);
    }
}
