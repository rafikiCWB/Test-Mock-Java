package br.com.alura.leilao.service;

import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class GeradorDePagamentoTest {

    private GeradorDePagamento gerador;

    @Mock
    private PagamentoDao pagamentoDao;

    @Captor
    private ArgumentCaptor<Pagamento> captor;

    @Mock
    private Clock clock;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        this.gerador = new GeradorDePagamento(pagamentoDao, clock);
    }

    @org.junit.jupiter.api.Test
    public void deveriaCriarPagamentoParaVencedorDoleilao() {
        Leilao leilao = leilao();
        Lance vencedor = leilao.getLances().get(0);

        LocalDate data = LocalDate.of(2021, 4, 3);

        //Pega a data que é um localData com o inicio do dia pra virar um localDate Time pega um zoneId do proprio sistema e ai converte em um instant;
        Instant instant = data.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant()).thenReturn(instant);
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        gerador.gerarPagamento(vencedor);

        //verifica se nosso pagamentoDao teve o metodo salvar chamado;
        Mockito.verify(pagamentoDao).salvar(captor.capture());

        //pega o objeto que foi capiturado
        Pagamento pagamento = captor.getValue();

        Assert.assertEquals(LocalDate.now().plusDays(1),
                pagamento.getVencimento());
        Assert.assertEquals(vencedor.getValor(), (pagamento.getValor()));
        Assert.assertFalse(pagamento.getPago());
        Assert.assertEquals(vencedor.getUsuario(), pagamento.getUsuario());
        Assert.assertEquals(leilao, pagamento.getLeilao());
    }

    private Leilao leilao() {
        Leilao leilao = new Leilao("Celular",
                new BigDecimal("500"),
                new Usuario("fulano"));

        Lance lance = new Lance(new Usuario("Beltrano"),
                new BigDecimal("600"));

        leilao.propoe(lance);
        leilao.setLanceVencedor(lance);

        return leilao;
    }
}
