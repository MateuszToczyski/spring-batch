package com.kodilla.springbatch;

import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DataProcessor implements ItemProcessor<InputData, OutputData> {

	@Override
	public OutputData process(final InputData item) {
		final int age = calculateAge(item.getBirthDate());
		return new OutputData(item.getFirstName(), item.getLastName(), age);
	}

	private int calculateAge(final String birthDateStr) {
		Objects.requireNonNull(birthDateStr, "birthDateStr must not be null");

		final LocalDate birthDate = parseBirthDate(birthDateStr);
		final LocalDate now = LocalDate.now();
		if (birthDate.isAfter(now)) {
			throw new IllegalArgumentException("birthDate must not be later than today");
		}
		return Period.between(birthDate, now).getYears();
	}

	private LocalDate parseBirthDate(final String birthDateStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return LocalDate.parse(birthDateStr, formatter);
	}
}
