package midas.SoundOfFlower.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DateRequest {

    private Long startYear;
    private Long startMonth;
    private Long startDay;

    private Long endYear;
    private Long endMonth;
    private Long endDay;
}
