package br.com.chai.domain;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ContractTest {

    Contract c1, c2, c3, c4;

    @Before
    public void setup(){
        c1 = new Contract();
        c1.setInicioConsumoROL("01.01.2011");   //01.01.2011 00:00:00
        c1.setFimConsumoROL("31.03.2013");      //31.03.2013 23:59:59
        c1.setVlrFixo("2.665.205,97");

        c2 = new Contract();
        c2.setInicioConsumoROL("01.01.2011");
        c2.setFimConsumoROL("31.12.2013");
        c2.setVlrFixo("390.870,88");

        c3 = new Contract();
        c3.setInicioConsumoROL("01.01.2011");
        c3.setFimConsumoROL("04.04.2013");
        c3.setVlrFixo("546.948,54");

        c4 = new Contract();
        c4.setInicioConsumoROL("01.10.2012");
        c4.setFimConsumoROL("30.09.2015");
        c4.setVlrFixo("9.673.926,20");
    }

    @Test
    public void getQuantidadeTotalMeses(){
        assertThat(c1.getQuantidadeTotalMeses(), is(new BigDecimal("26.97")));
        assertThat(c2.getQuantidadeTotalMeses(), is(new BigDecimal("35.97")));
        assertThat(c3.getQuantidadeTotalMeses(), is(new BigDecimal("27.13"))); // 28
        assertThat(c4.getQuantidadeTotalMeses(), is(new BigDecimal("35.97")));
    }

    @Test
    public void getConsumoMes(){
        assertThat(c1.getConsumoMes(), is(new BigDecimal("98821.14")));
        assertThat(c2.getConsumoMes(), is(new BigDecimal("10866.58")));
        assertThat(c3.getConsumoMes(), is(new BigDecimal("20160.29"))); // 19533.88
        assertThat(c4.getConsumoMes(), is(new BigDecimal("268944.30")));
    }

    @Test
    public void getYearConsumption(){
        List<YearConsumption> years1 = c1.getYearConsumption();
        assertThat(years1.size(), is(3));
        assertThat(years1, hasItem(equalTo(new YearConsumption(2011, new BigDecimal("12.00"), new BigDecimal("1185853.68")))));
        assertThat(years1, hasItem(equalTo(new YearConsumption(2012, new BigDecimal("12.00"), new BigDecimal("1185853.68")))));
        assertThat(years1, hasItem(equalTo(new YearConsumption(2013, new BigDecimal("2.97"), new BigDecimal("293498.79")))));


        List<YearConsumption> years3 = c3.getYearConsumption();
        assertThat(years3.size(), is(3));
        assertThat(years3, hasItem(equalTo(new YearConsumption(2011, new BigDecimal("12.00"), new BigDecimal("241923.48"))))); // 234.406,52
        assertThat(years3, hasItem(equalTo(new YearConsumption(2012, new BigDecimal("12.00"), new BigDecimal("241923.48"))))); // 234.406,52
        assertThat(years3, hasItem(equalTo(new YearConsumption(2013, new BigDecimal("3.13"), new BigDecimal("63101.71")))));   // 78.135,50571 - 4 meses (� o que fez a diferen�a)


        List<YearConsumption> years4 = c4.getYearConsumption();
        assertThat(years4.size(), is(4));
        assertThat(years4, hasItem(equalTo(new YearConsumption(2012, new BigDecimal("3.00"), new BigDecimal("806832.90")))));
        assertThat(years4, hasItem(equalTo(new YearConsumption(2013, new BigDecimal("12.00"), new BigDecimal("3227331.60")))));
        assertThat(years4, hasItem(equalTo(new YearConsumption(2014, new BigDecimal("12.00"), new BigDecimal("3227331.60")))));
        assertThat(years4, hasItem(equalTo(new YearConsumption(2015, new BigDecimal("8.97"), new BigDecimal("2412430.38"))))); // n�o tinha calculado o 2015
    }
}
