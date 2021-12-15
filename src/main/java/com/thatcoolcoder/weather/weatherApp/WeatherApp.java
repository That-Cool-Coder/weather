package com.thatcoolcoder.weather.weatherApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.thatcoolcoder.weather.common.*;
import com.thatcoolcoder.weather.weatherApi.*;
import com.thatcoolcoder.weather.weatherApi.exceptions.*;
import com.thatcoolcoder.weather.weatherApi.models.*;

public class WeatherApp extends JFrame {
    private WeatherService weatherService;
    private WeatherDisplayPanel weatherDisplayPanel = new WeatherDisplayPanel();

    public WeatherApp(WeatherService weatherService)
    {
        super("Weather by ThatCoolCoder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        useSystemLookAndFeel();
        
        this.weatherService = weatherService;
        TopBar topBar = new TopBar((String location) -> showWeather(location));
        add(topBar, BorderLayout.NORTH);
        add(weatherDisplayPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent we)
            {
                if (Config.current.autoOpenLastLocation && ! Config.current.locationLastVisited.isEmpty())
                {
                    showWeather(Config.current.locationLastVisited);
                }
            }

            public void windowClosing(WindowEvent we)
            {
                try
                {
                    Config.save();
                }
                catch (Exception e)
                {
                    // do nothing - saving config is not a major error
                }
            }
        });
    }

    private void useSystemLookAndFeel()
    {
        try
        {
            String lookAndFeelName;
            switch (OS.getOS())
            {
                case WINDOWS:
                    lookAndFeelName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
                    break;
                case LINUX:
                    lookAndFeelName = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                    break;
                default:
                    lookAndFeelName = "javax.swing.plaf.metal.MetalLookAndFeel";
            }
            UIManager.setLookAndFeel(lookAndFeelName);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Exception e)
        {
            System.err.println("Failed to use native look and theme");
        }
    }

    private void showWeather(String location)
    {
        try
        {
            Config.current.locationLastVisited = location;
            weatherService.apiKey = Config.current.weatherApiKey;
            WeatherSnapshot weatherSnapshot = weatherService.getCurrentWeather(location);
            weatherDisplayPanel.showWeather(weatherSnapshot);
        }
        catch (InvalidApiKeyException e)
        {
            if (Config.current.weatherApiKey.isEmpty())
            {
                UIUtils.showException(this, "No API key set. Set one in the settings menu.");
            }
            else
            {
                UIUtils.showException(this, "Invalid API key.");
            }
        }
        catch (NoLocationProvidedException e)
        {
            // do nothing
        }
        catch (InvalidLocationException e)
        {
            UIUtils.showException(this, e.getMessage());
        }
        catch (Exception e)
        {
            UIUtils.showException(this, e, "fetching weather data");
        }
    }
}
