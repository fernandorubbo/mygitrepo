package br.datamaio.fly.check.gol;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.TemporalAdjusters.next;

import static br.datamaio.fly.DayPeriod.AFTERNOON;
import static br.datamaio.fly.DayPeriod.AFTERNOON_OR_NIGHT;
import static br.datamaio.fly.DayPeriod.MORNING;
import static br.datamaio.fly.DayPeriod.NIGHT;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import br.datamaio.fly.DayPeriod;
import br.datamaio.fly.Option;
import br.datamaio.fly.Schedule;
import br.datamaio.fly.check.gol.pages.SearchPage;
import br.datamaio.fly.check.gol.pages.SelectFlyPage;

public class VoeGolCheck {

    //private static final LocalDate START_DATE = LocalDate.now();
    private static final LocalDate START_DATE = LocalDate.of(2014, 6, 25);
    private static final int PERIOD_IN_MONTH = 3;

    private static final String CONGONHAS = "Congonhas";
    private static final String CAXIAS = "Caxias do Sul";
    private static final DateTimeFormatter DATE = ofPattern("dd/MM/yyyy");
    private static final NumberFormat REAIS = DecimalFormat.getCurrencyInstance();

    private static WebDriver driver;

    @BeforeClass
    public static void setUp(){
        Path f = Paths.get("chromedriver.exe");
        System.setProperty("webdriver.chrome.driver",f.toAbsolutePath().toString());
        driver = new ChromeDriver();
    }

    @AfterClass
    public static void tearDown(){
        driver.quit();
    }

    @Test
    public void congonhas2caxias() throws Exception {
        Period period = Period.ofMonths(PERIOD_IN_MONTH);
        LocalDate fromDate = START_DATE;
        LocalDate untilDate = fromDate.plus(period);

        Path logFile = buildLogFile("congonhas2caxias_");
        try (BufferedWriter writter = Files.newBufferedWriter(logFile)) {

            write(writter, String.format("Searching Flyies from '%s' to '%s' ", fromDate.format(DATE), untilDate.format(DATE)));

            LocalDate next = fromDate;
            do {
                LocalDate friday    = next.with(next(DayOfWeek.FRIDAY));
                LocalDate saturday  = friday.with(next(DayOfWeek.SATURDAY));
                LocalDate sunday    = friday.with(next(DayOfWeek.SUNDAY));
                LocalDate monday    = friday.with(next(DayOfWeek.MONDAY));

                check(writter, CONGONHAS, CAXIAS, friday, AFTERNOON, sunday, NIGHT);
                check(writter, CONGONHAS, CAXIAS, friday, AFTERNOON, monday, MORNING);

                check(writter, CONGONHAS, CAXIAS, saturday, MORNING, sunday, NIGHT);
                check(writter, CONGONHAS, CAXIAS, saturday, MORNING, monday, MORNING);

                write(writter, "");
                write(writter, "");

                next = monday;
            } while(next.compareTo(untilDate)<0);
        }
    }


    @Test
    public void caxias2congonhas() throws Exception {
        Period period = Period.ofMonths(PERIOD_IN_MONTH);
        LocalDate fromDate = START_DATE;
        LocalDate untilDate = fromDate.plus(period);

        Path logFile = buildLogFile("caxias2congonhas_");
        try (BufferedWriter writter = Files.newBufferedWriter(logFile)) {

            write(writter, String.format("Searching Flyies from '%s' to '%s' ", fromDate.format(DATE), untilDate.format(DATE)));

            LocalDate next = fromDate;
            do {
                LocalDate friday    = next.with(next(DayOfWeek.FRIDAY));
                LocalDate saturday  = friday.with(next(DayOfWeek.SATURDAY));
                LocalDate sunday    = friday.with(next(DayOfWeek.SUNDAY));
                LocalDate monday    = friday.with(next(DayOfWeek.MONDAY));

                check(writter, CAXIAS, CONGONHAS, friday, NIGHT, sunday, AFTERNOON_OR_NIGHT);
                check(writter, CAXIAS, CONGONHAS, friday, NIGHT, monday, MORNING);

                check(writter, CAXIAS, CONGONHAS, saturday, MORNING, sunday, AFTERNOON_OR_NIGHT);
                check(writter, CAXIAS, CONGONHAS, saturday, MORNING, monday, MORNING);

                write(writter, "");
                write(writter, "");

                next = monday;
            } while(next.compareTo(untilDate)<0);
        }
    }

    @Test
    public void caxias2congonhas_apenasida() throws Exception {
        Period period = Period.ofDays(8);
        LocalDate fromDate = LocalDate.of(2014,06,01);
        LocalDate untilDate = fromDate.plus(period);

        Path logFile = buildLogFile("caxias2congonhas_apenasida_");
        try (BufferedWriter writter = Files.newBufferedWriter(logFile)) {

            write(writter, String.format("Searching Flyies from '%s' to '%s' ", fromDate.format(DATE), untilDate.format(DATE)));

            LocalDate next = fromDate;
            do {
                SearchPage search = new SearchPage(driver).navigate()
                        .selectOneWay().from(CAXIAS).to(CONGONHAS)
                        .departure(next);
                SelectFlyPage selectFly = search.buy();
                Option o = selectFly.getBestDepartureOption();

                if (o != null ) {
                    write(writter, "==============================================================================================");
                    write(writter, String.format("%s -> %s", CAXIAS, CONGONHAS));
                    Schedule s = o.getSchedule();
                    write(writter, String.format("\tIDA     : %s dia %s (%s - %s): %s", next.getDayOfWeek(), next.format(DATE), s.getTakeoffTime(), s.getLandingTime(), o));
                    write(writter, String.format("\t** TOTAL ** : %s", REAIS.format(o.getValue())));
                }

                next = next.plus(1, DAYS);
            } while(next.compareTo(untilDate)<0);
        }
    }

    // ------------ methodos privados -------------

    private Path buildLogFile(final String filePrefix) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        Path dir =  Paths.get("reports", now.format(ofPattern("yyyyMMdd")));
        Files.createDirectories(dir);
        return Paths.get(dir.toString(), filePrefix + now.format(ofPattern("HHmm")) + ".txt");
    }

    private void check(final BufferedWriter writter, final String from, final String to,
            final LocalDate ddep, final DayPeriod pdep,
            final LocalDate dret, final DayPeriod pret) throws Exception {

        SearchPage search = new SearchPage(driver).navigate()
                .selectRoundTrip().from(from).to(to)
                .departure(ddep).returning(dret);
        SelectFlyPage selectFly = search.buy();
        Option od = selectFly.getBestDepartureOption(pdep);
        Option or = selectFly.getBestReturningOption(pret);
        if (od == null || or == null) {
            return;
        }

        write(writter, "==============================================================================================");
        write(writter, String.format("%s -> %s", from, to));
        Schedule sd = od.getSchedule();
        Schedule sr = or.getSchedule();
        write(writter, String.format("\tIDA     : %s dia %s (%s - %s): %s", ddep.getDayOfWeek(), ddep.format(DATE), sd.getTakeoffTime(), sd.getLandingTime(), od));
        write(writter, String.format("\tVOLTA   : %s dia %s (%s - %s): %s", dret.getDayOfWeek(), dret.format(DATE), sr.getTakeoffTime(), sr.getLandingTime(), or));
        write(writter, String.format("\t** TOTAL ** : %s", REAIS.format(od.getValue().add(or.getValue())) ));
    }


    private void write(final BufferedWriter writter, final String msg) throws IOException {
        System.out.println(msg);
        writter.write(msg);
        writter.newLine();
        writter.flush();
    }
}
