package com.seuprojeto.marketplace.application.usecase;

import com.seuprojeto.marketplace.application.dto.SelecaoCarrinho;
import com.seuprojeto.marketplace.domain.model.CategoriaProduto;
import com.seuprojeto.marketplace.domain.model.Produto;
import com.seuprojeto.marketplace.domain.repository.ProdutoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CalcularCarrinhoE2ETest {

    private CalcularCarrinhoUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new CalcularCarrinhoUseCase(new FakeProdutoRepositorio());
    }

    // Cenário 1: Um único item (PELICULA)
    // Desconto qtd=0%, cat=2%, total=2%  → desconto=0,60  total=29,40
    @Test
    void cenario1_umItemPelicula() {
        var result = useCase.executar(List.of(new SelecaoCarrinho(4L, 1)));

        assertEquals(0, new BigDecimal("30.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("0.60").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("29.40").compareTo(result.getTotal()));
    }

    // Cenário 2: Um único item (CAPINHA)
    // Desconto qtd=0%, cat=3%, total=3%  → desconto=1,50  total=48,50
    @Test
    void cenario2_umItemCapinha() {
        var result = useCase.executar(List.of(new SelecaoCarrinho(1L, 1)));

        assertEquals(0, new BigDecimal("50.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("1.50").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("48.50").compareTo(result.getTotal()));
    }

    // Cenário 3: Dois itens de categorias diferentes (Película + Suporte)
    // Desconto qtd=5%, cat=4%, total=9%  → desconto=9,90  total=100,10
    @Test
    void cenario3_doisItensCategoriasDiferentes() {
        var result = useCase.executar(List.of(
                new SelecaoCarrinho(4L, 1),
                new SelecaoCarrinho(5L, 1)
        ));

        assertEquals(0, new BigDecimal("110.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("9.90").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("100.10").compareTo(result.getTotal()));
    }

    // Cenário 4: Três itens (Capinha + Película + Suporte)
    // Desconto qtd=7%, cat=7%, total=14%  → desconto=22,40  total=137,60
    @Test
    void cenario4_tresItens() {
        var result = useCase.executar(List.of(
                new SelecaoCarrinho(1L, 1),
                new SelecaoCarrinho(4L, 1),
                new SelecaoCarrinho(5L, 1)
        ));

        assertEquals(0, new BigDecimal("160.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("22.40").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("137.60").compareTo(result.getTotal()));
    }

    // Cenário 5: Quatro itens — desconto máximo NÃO atingido
    // Capinha + Película + Suporte + Fone
    // Desconto qtd=10%, cat=10%, total=20%  → desconto=72,00  total=288,00
    @Test
    void cenario5_quatroItensDescontoNaoAtingido() {
        var result = useCase.executar(List.of(
                new SelecaoCarrinho(1L, 1),
                new SelecaoCarrinho(4L, 1),
                new SelecaoCarrinho(5L, 1),
                new SelecaoCarrinho(3L, 1)
        ));

        assertEquals(0, new BigDecimal("360.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("72.00").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("288.00").compareTo(result.getTotal()));
    }

    // Cenário 6: Quatro itens — desconto máximo NÃO atingido (23% < 25%)
    // Carregador + Fone + Capinha + Película
    // Desconto qtd=10%, cat=13%, total=23%  → desconto=87,40  total=292,60
    @Test
    void cenario6_quatroItensDescontoAte23Porcento() {
        var result = useCase.executar(List.of(
                new SelecaoCarrinho(2L, 1),
                new SelecaoCarrinho(3L, 1),
                new SelecaoCarrinho(1L, 1),
                new SelecaoCarrinho(4L, 1)
        ));

        assertEquals(0, new BigDecimal("380.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("87.40").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("292.60").compareTo(result.getTotal()));
    }

    // Cenário 7: Cinco itens — limite de 25% atingido exatamente
    // Carregador + Fone + Capinha + Película + Suporte
    // Desconto qtd=10%, cat=15%, total=25%  → desconto=115,00  total=345,00
    @Test
    void cenario7_cincoItensLimite25Porcento() {
        var result = useCase.executar(List.of(
                new SelecaoCarrinho(2L, 1),
                new SelecaoCarrinho(3L, 1),
                new SelecaoCarrinho(1L, 1),
                new SelecaoCarrinho(4L, 1),
                new SelecaoCarrinho(5L, 1)
        ));

        assertEquals(0, new BigDecimal("460.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("115.00").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("345.00").compareTo(result.getTotal()));
    }

    // Cenário 8: Dois itens da mesma categoria (2x Capinha)
    // Desconto qtd=5%, cat=6% (3%×2), total=11%  → desconto=11,00  total=89,00
    @Test
    void cenario8_doisItensMesmaCategoria() {
        var result = useCase.executar(List.of(new SelecaoCarrinho(1L, 2)));

        assertEquals(0, new BigDecimal("100.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("11.00").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("89.00").compareTo(result.getTotal()));
    }

    // Cenário 9: Três itens com categorias repetidas (2x Película + 1x Suporte)
    // Desconto qtd=7%, cat=6% (2%×2 + 2%×1), total=13%  → desconto=18,20  total=121,80
    @Test
    void cenario9_tresItensCategoriasRepetidas() {
        var result = useCase.executar(List.of(
                new SelecaoCarrinho(4L, 2),
                new SelecaoCarrinho(5L, 1)
        ));

        assertEquals(0, new BigDecimal("140.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("18.20").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("121.80").compareTo(result.getTotal()));
    }

    // Cenário 10: Produto mais caro (1x Fone)
    // Desconto qtd=0%, cat=3%, total=3%  → desconto=6,00  total=194,00
    @Test
    void cenario10_umItemFone() {
        var result = useCase.executar(List.of(new SelecaoCarrinho(3L, 1)));

        assertEquals(0, new BigDecimal("200.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("6.00").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("194.00").compareTo(result.getTotal()));
    }

    // Cenário 11: Limite 25% com múltiplos carregadores (4x Carregador)
    // Desconto qtd=10%, cat=20% (5%×4), total calculado=30% → aplicado=25%
    // desconto=100,00  total=300,00
    @Test
    void cenario11_quatroCarregadoresLimite25Porcento() {
        var result = useCase.executar(List.of(new SelecaoCarrinho(2L, 4)));

        assertEquals(0, new BigDecimal("400.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("300.00").compareTo(result.getTotal()));
    }

    // Cenário 12: Dois produtos de categorias com desconto baixo (Película + Suporte)
    // Desconto qtd=5%, cat=4%, total=9%  → desconto=9,90  total=100,10
    @Test
    void cenario12_doisProdutosDescontoBaixo() {
        var result = useCase.executar(List.of(
                new SelecaoCarrinho(4L, 1),
                new SelecaoCarrinho(5L, 1)
        ));

        assertEquals(0, new BigDecimal("110.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("9.90").compareTo(result.getDesconto()));
        assertEquals(0, new BigDecimal("100.10").compareTo(result.getTotal()));
    }

    // Repositório fake com os mesmos dados do InMemoryProdutoRepositorio
    static class FakeProdutoRepositorio implements ProdutoRepositorio {

        private final List<Produto> produtos = List.of(
                new Produto(1L, "Capinha Premium",            CategoriaProduto.CAPINHA,    new BigDecimal("50.00")),
                new Produto(2L, "Carregador Turbo 30W",       CategoriaProduto.CARREGADOR, new BigDecimal("100.00")),
                new Produto(3L, "Fone Bluetooth AirSound",    CategoriaProduto.FONE,       new BigDecimal("200.00")),
                new Produto(4L, "Película 3D",                CategoriaProduto.PELICULA,   new BigDecimal("30.00")),
                new Produto(5L, "Suporte Veicular Magnético", CategoriaProduto.SUPORTE,    new BigDecimal("80.00"))
        );

        @Override
        public List<Produto> findAll() { return produtos; }

        @Override
        public Optional<Produto> findById(Long id) {
            return produtos.stream().filter(p -> p.getId().equals(id)).findFirst();
        }
    }
}