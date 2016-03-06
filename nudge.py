#!/usr/bin/python

class Route:
    def __init__(self, start, end):
        self.start = start
        self.end = end

jeffco = Route('KPAE', '0S9')

class PilotLog:
    def __init__(self, medicalExpiration, passengerCurrencyExpiration):
        self.medicalExpiration = medicalExpiration
        self.passengerCurrencyExpiration = passengerCurrencyExpiration

class Availability:
    def __init__(self, calendar):
        self.calendar = calendar

    def available_on(self, date):
        # self.calendar.eventsOn(date).size == 0
        # Figure this out
        True

class SeatOccupant:
    def __init__(self, name, weight, availability):
        self.name = name
        self.weight = weight
        self.availability = availability

class Airplane:
    def __init__(self, tailNumber, availability, performance, cost):
        self.tailNumber = tailNumber
        self.availability = availability
        self.performance = performance
        self.cost = cost

class Weather:
    def __init__(self, timestamp, altitude, wind, temperature):
        self.timestamp = timestamp
        self.altitude = altitude
        self.wind = wind
        self.temperature = temperature

class Metar:
    def __init__(self, timestamp, station, winds, temp, dew, clouds):
        self.timestamp = timestamp
        # TODO

class WeatherMinimum:
    def __init__(self, constraint):
        self.constraint = constraint

    def satisfied(self, wx):
        self.constraint(wx)

    def andAlso(self, constraint):
        WeatherMinimum(lambda wx: self.constraint(wx) and constraint(wx))

print('yo dog')
