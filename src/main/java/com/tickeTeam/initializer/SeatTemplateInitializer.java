package com.tickeTeam.initializer;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.seat.entity.SeatInfo;
import com.tickeTeam.domain.seat.entity.SeatTemplate;
import com.tickeTeam.domain.seat.entity.SeatType;
import com.tickeTeam.domain.seat.repository.SeatTemplateRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@RequiredArgsConstructor
@Profile("!test")
public class SeatTemplateInitializer implements ApplicationRunner {

    private final SeatTemplateRepository seatTemplateRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<SeatTemplate> seatTemplates = new ArrayList<>();

        // CSV 파일 경로
        Path filePath = Paths.get("src/main/resources/data/seat_templates.csv");
        try(CSVReader csvReader = new CSVReader(new FileReader(filePath.toFile()))){

            List<String[]> rows = csvReader.readAll();

            // 각 행을 읽어서 Game 엔티티로 변환
            for (String[] row : rows) {
                SeatType seatType = SeatType.valueOf(row[0].toUpperCase());
                String seatSection = row[1];
                String seatBlock = row[2];
                Integer seatRowInt = (row[3] != null && !row[3].isEmpty()) ? Integer.parseInt(row[3]) : null;
                Integer seatNumInt = (row[4] != null && !row[4].isEmpty()) ? Integer.parseInt(row[4]) : null;

                SeatTemplate newSeatTemplate = buildSeatTemplate(seatType, seatSection, seatBlock, seatRowInt, seatNumInt);

                seatTemplates.add(newSeatTemplate);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.GAME_DATA_INSERT_ERROR);
        }

        seatTemplateRepository.saveAll(seatTemplates);
    }

    // 기존 buildSeatTemplate 메소드는 그대로 사용하거나, CSV 구조에 맞게 파라미터 조정 가능
    private SeatTemplate buildSeatTemplate(SeatType seatType, String section, String block, Integer row, Integer num) {
        return SeatTemplate.builder()
                .seatInfo(SeatInfo.builder()
                        .seatType(seatType)
                        .seatSection(section)
                        .seatBlock(block)
                        .seatRow(row != null ? String.valueOf(row) : null)
                        .seatNum(num)
                        .build())
                .build();
    }
}
