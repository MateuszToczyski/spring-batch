package com.kodilla.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
	}

	@Bean
	FlatFileItemReader<InputData> reader() {
		FlatFileItemReader<InputData> reader = new FlatFileItemReader<>();
		reader.setResource(new ClassPathResource("input.csv"));

		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames("firstName", "lastName", "birthDate");

		BeanWrapperFieldSetMapper<InputData> mapper = new BeanWrapperFieldSetMapper<>();
		mapper.setTargetType(InputData.class);

		DefaultLineMapper<InputData> lineMapper = new DefaultLineMapper<>();
		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(mapper);

		reader.setLineMapper(lineMapper);
		return reader;
	}

	@Bean
	DataProcessor processor() {
		return new DataProcessor();
	}

	@Bean
	FlatFileItemWriter<OutputData> writer() {
		BeanWrapperFieldExtractor<OutputData> extractor = new BeanWrapperFieldExtractor<>();
		extractor.setNames(new String[] { "firstName", "lastName", "age" });

		DelimitedLineAggregator<OutputData> aggregator = new DelimitedLineAggregator<>();
		aggregator.setDelimiter(",");
		aggregator.setFieldExtractor(extractor);

		FlatFileItemWriter<OutputData> writer = new FlatFileItemWriter<>();
		writer.setResource(new FileSystemResource("output.csv"));
		writer.setShouldDeleteIfExists(true);
		writer.setLineAggregator(aggregator);

		return writer;
	}

	@Bean
	Step priceChange(
			ItemReader<InputData> reader, ItemProcessor<InputData, OutputData> processor,
			ItemWriter<OutputData> writer) {

		return stepBuilderFactory
				.get("calculateAge")
				.<InputData, OutputData>chunk(100)
				.reader(reader)
				.processor(processor)
				.writer(writer)
				.build();
	}

	@Bean
	Job changePriceJob(Step priceChange) {
		return jobBuilderFactory
				.get("calculateAgeJob")
				.incrementer(new RunIdIncrementer())
				.flow(priceChange)
				.end()
				.build();
	}
}
