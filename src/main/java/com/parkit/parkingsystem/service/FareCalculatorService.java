package com.parkit.parkingsystem.service;

import java.util.concurrent.TimeUnit;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        TicketDAO ticketDAO = new TicketDAO();

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        //TODO:_END Some tests are failing here. Need to check if this logic is correct
        TimeUnit unitTime = TimeUnit.MINUTES;
        double duration = (double) unitTime.convert(outTime - inTime, TimeUnit.MILLISECONDS)/60;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice((duration > 0.5 ? 0 : duration) * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice((duration > 0.5 ? 0 : duration) * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        calculateFare(ticket, ticketDAO.getNbTicket(ticket.getVehicleRegNumber())>0);
    }

    private void calculateFare(Ticket ticket, Boolean discount){
        if(discount) ticket.setPrice(ticket.getPrice() * 0.95);
    }
}