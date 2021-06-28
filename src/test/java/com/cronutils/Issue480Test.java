package com.cronutils;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.cronutils.model.field.expression.FieldExpression.questionMark;
import static com.cronutils.model.field.expression.FieldExpressionFactory.always;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.fail;

public class Issue480Test {

    private static final Logger LOGGER = LoggerFactory.getLogger(Issue480Test.class);

    private static final CronDefinition definition = CronDefinitionBuilder.defineCron()
            .withMinutes().and()
            .withHours().and()
            .withDayOfWeek().supportsQuestionMark().and()
            .withDayOfMonth().supportsL().supportsQuestionMark().and()
            .withDayOfYear().supportsQuestionMark().and()
            .withMonth().and()
            .withYear().optional().withValidRange(1970, 2099).and()
            .matchDayOfWeekAndDayOfMonth()
            .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
            .withCronValidation(CronConstraintsFactory.ensureEitherDayOfYearOrMonth())
            .instance();

    @Test
    @Ignore // TODO broken??
    public void testIntervalsEvery5thMonthsSinceASpecificMonth() {
        LocalDateTime sunday = LocalDateTime.of(2021, 6, 27, 0, 0);
        Assert.assertEquals(sunday.getDayOfWeek(), DayOfWeek.SUNDAY);
        Clock clock = Clock.fixed(sunday.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now(clock);
        LOGGER.info("Now: {}", now);

        Cron cron = getWeekly(now).instance();
        ZonedDateTime nextRun;

        final ZonedDateTime nowPlusWeek = now.plusWeeks(1);
        LOGGER.info("now + 1 week: {}", nowPlusWeek);

        nextRun = nextRun(cron, now); // first run
        LOGGER.info("nextRun: {}", nextRun);

        assertTrue(nextRun.truncatedTo(ChronoUnit.MINUTES)
                .isEqual(nowPlusWeek.truncatedTo(ChronoUnit.MINUTES)));
    }

    private CronBuilder getWeekly(ZonedDateTime now) {
        return CronBuilder.cron(definition)
                .withMinute(on(now.getMinute()))
                .withHour(on(now.getHour()))
                .withDoW(on(now.getDayOfWeek().ordinal())) // ordinal -- 0 to 6
//              .withDoW(on(now.getDayOfWeek().getValue())) // value -- 1 to 7
                .withDoM(questionMark())
                .withDoY(questionMark())
                .withMonth(always())
                .withYear(always());
    }

    private static ZonedDateTime nextRun(Cron cron, ZonedDateTime when) {
        final Optional<ZonedDateTime> next = ExecutionTime.forCron(cron).nextExecution(when);
        if (!next.isPresent()) {
            fail();
        }
        System.out.println("Calculated next run at " + next.get());
        return next.get();
    }

}
