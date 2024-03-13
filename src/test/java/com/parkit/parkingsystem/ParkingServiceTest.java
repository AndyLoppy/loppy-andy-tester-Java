package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ParkingServiceTest {

    private static ParkingService parkingService;
    private static InputReaderUtil inputReaderUtil;
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        inputReaderUtil = mock(InputReaderUtil.class);
        parkingSpotDAO = mock(ParkingSpotDAO.class);
        ticketDAO = mock(TicketDAO.class);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processIncomingVehicleTest() throws Exception {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        // Act
        parkingService.processIncomingVehicle();

        // Assert
        verify(parkingSpotDAO, times(1)).updateParking(parkingSpot);
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Act
        parkingService.processExitingVehicle();

        // Assert
        verify(ticketDAO, times(1)).updateTicket(ticket);
        verify(parkingSpotDAO, times(1)).updateParking(ticket.getParkingSpot());
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Act
        parkingService.processExitingVehicle();

        // Assert
        verify(ticketDAO, times(1)).updateTicket(ticket);
        verify(parkingSpotDAO, never()).updateParking(ticket.getParkingSpot());
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1); // simulate user input 1 for vehicle type
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1); // simulate available parking spot with ID 1

        // Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNotNull(parkingSpot); // verify that the method returns a valid parking spot
        assertEquals(1, parkingSpot.getId()); // verify that the ID of the obtained parking spot is 1
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1); // simulate user input 1 for vehicle type
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1); // simulate no available parking spot

        // Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNull(parkingSpot); // verify that the method returns null when no parking spot is available
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(3); // input 3, wrong

        // Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNull(parkingSpot);
    }
}