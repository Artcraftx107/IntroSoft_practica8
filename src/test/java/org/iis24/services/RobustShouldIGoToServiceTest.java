package org.iis24.services;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedList;


public class RobustShouldIGoToServiceTest {

    private RobustShouldIGoToService robustShouldIGoToService;
    private IMapService mapServiceMock;
    private IWeatherService weatherServiceMock;
    //private IMapService mapServiceMock2;
    //private IWeatherService weatherServiceMock2;

    @BeforeEach
     void setUp() {
        mapServiceMock = mock(IMapService.class);
        weatherServiceMock = mock(IWeatherService.class);
        //mapServiceMock2 = mock(IMapService.class);
        //weatherServiceMock2 = mock(IWeatherService.class);
        LinkedList<IMapService> mapServices = new LinkedList<>();
        mapServices.add(mapServiceMock);
        //mapServices.add(mapServiceMock2);
        LinkedList<IWeatherService> weatherServices = new LinkedList<>();
        weatherServices.add(weatherServiceMock);
        //weatherServices.add(weatherServiceMock2);
        robustShouldIGoToService = new RobustShouldIGoToService(mapServices, weatherServices);
    }
    @AfterEach
     void tearDown(){
        robustShouldIGoToService=null;
        mapServiceMock=null;
        weatherServiceMock=null;
    }
    @Test
     void shouldRaiseInvalidServiceInstanceExceptionWhenListsContainNullReferences(){
        LinkedList<IMapService> list = new LinkedList<>();
        list.add(null);
        LinkedList<IWeatherService> testList = new LinkedList<>();
        assertThrows(InvalidServiceInstanceException.class, () -> new RobustShouldIGoToService(list, testList));
    }
    @Test
     void shouldRaiseInvalidMapServiceExecutionExceptionIfMapServiceFails(){
        when(mapServiceMock.getCoordinates(anyString())).thenThrow(new InvalidMapServiceExecutionException("The service has failed"));
        assertThrows(InvalidMapServiceExecutionException.class, ()->robustShouldIGoToService.shouldIGoTo("Example", LocalDate.now()));
    }
    @Test
     void shouldInvokeWeatherServiceWithValidCoordinatesIfOneMapServiceWorksWell(){
        GPSCoordinates coords = new GPSCoordinates(0, 0);
        when(mapServiceMock.getCoordinates(anyString())).thenReturn(coords);
        robustShouldIGoToService.shouldIGoTo("Example", LocalDate.now());
        verify(weatherServiceMock, times(1)).rainProbability(eq(coords), any(LocalDate.class));
        verify(weatherServiceMock, times(1)).totalAmountOfRain(eq(coords), any(LocalDate.class));
    }
    @Test
     void shouldReturnTrueWhenAverageOfProbabilitiesAndTotalAmountOfRainAreZero(){
        when(weatherServiceMock.rainProbability(any(GPSCoordinates.class), any(LocalDate.class))).thenReturn(0.0);
        when(weatherServiceMock.totalAmountOfRain(any(GPSCoordinates.class), any(LocalDate.class))).thenReturn(0.0);
        assertTrue(robustShouldIGoToService.shouldIGoTo("Example", LocalDate.now()));
    }
    @Test
     void shouldReturnFalseWhenAverageOfProbabilitiesAndTotalAmountOfRainAreEqualToThresholds() {
        GPSCoordinates coords = new GPSCoordinates(0,0);
        when(mapServiceMock.getCoordinates(anyString())).thenReturn(coords);
        robustShouldIGoToService.setRainProbabilityThreshold(50);
        robustShouldIGoToService.setRainAmountThreshold(10);
        when(weatherServiceMock.rainProbability(any(GPSCoordinates.class), any(LocalDate.class))).thenReturn(50.0);
        when(weatherServiceMock.totalAmountOfRain(any(GPSCoordinates.class), any(LocalDate.class))).thenReturn(10.0);
        /*Codigo para debugging del test
        System.out.println("Rain Probability Threshold: " + robustShouldIGoToService.getRainProbabilityThreshold());
        System.out.println("Rain Amount Threshold: " + robustShouldIGoToService.getRainAmountThreshold());
        **/
        assertFalse(robustShouldIGoToService.shouldIGoTo("Example", LocalDate.now()));
    }

    @Test
     void shouldInvokeAllAvailableWeatherServices(){
        GPSCoordinates coords = new GPSCoordinates(0, 0);
        when(mapServiceMock.getCoordinates(anyString())).thenReturn(coords);
        //when(mapServiceMock2.getCoordinates(anyString())).thenReturn(coords);
        robustShouldIGoToService.shouldIGoTo("Example", LocalDate.now());
        verify(weatherServiceMock, times(1)).rainProbability(eq(coords), any(LocalDate.class));
        verify(weatherServiceMock, times(1)).totalAmountOfRain(eq(coords), any(LocalDate.class));
        //verify(weatherServiceMock2, times(1)).rainProbability(eq(coords), any(LocalDate.class));
        //verify(weatherServiceMock2, times(1)).totalAmountOfRain(eq(coords), any(LocalDate.class));
    }
}
