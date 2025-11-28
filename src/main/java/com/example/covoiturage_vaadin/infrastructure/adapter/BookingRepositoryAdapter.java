package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.application.ports.IBookingRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Booking;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BookingRepositoryAdapter implements IBookingRepositoryPort {

    private final BookingJpaRepository jpaRepository;

    public BookingRepositoryAdapter(BookingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Booking save(Booking booking) {
        return jpaRepository.save(booking);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Booking> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Booking> findByStudentId(Long studentId) {
        return jpaRepository.findByStudentId(studentId);
    }

    @Override
    public List<Booking> findByTripId(Long tripId) {
        return jpaRepository.findByTripId(tripId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByTripIdAndStudentId(Long tripId, Long studentId) {
        return jpaRepository.existsByTripIdAndStudentId(tripId, studentId);
    }

    @Override
    public boolean existsActiveBookingByTripIdAndStudentId(Long tripId, Long studentId) {
        return jpaRepository.existsActiveBookingByTripIdAndStudentId(tripId, studentId);
    }
}
