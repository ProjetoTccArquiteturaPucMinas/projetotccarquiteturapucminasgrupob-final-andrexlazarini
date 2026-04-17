package com.seuprojeto.marketplace.application.usecase;

import com.seuprojeto.marketplace.application.dto.SelecaoCarrinho;
import com.seuprojeto.marketplace.domain.model.CategoriaProduto;
import com.seuprojeto.marketplace.domain.model.ItemCarrinho;
import com.seuprojeto.marketplace.domain.model.Produto;
import com.seuprojeto.marketplace.domain.model.ResumoCarrinho;
import com.seuprojeto.marketplace.domain.repository.ProdutoRepositorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class CalcularCarrinhoUseCase {

    private final ProdutoRepositorio produtoRepositorio;

    public CalcularCarrinhoUseCase(ProdutoRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
    }

    public ResumoCarrinho executar(List<SelecaoCarrinho> selecaoCarrinhos) {

        // Montar itens do carrinho
        List<ItemCarrinho> itens = selecaoCarrinhos.stream()
                .map(selecao -> {
                    Produto produto = produtoRepositorio.findById(selecao.getIdProduto())
                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + selecao.getIdProduto()));
                    return new ItemCarrinho(produto, selecao.getQuantidade());
                })
                .collect(Collectors.toList());

        // Calcular subtotal
        BigDecimal subtotal = itens.stream()
                .map(item -> item.getProduto().getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular total de itens (quantidade total)
        int totalItens = itens.stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();

        // Regra 1: Desconto por quantidade total de itens
        int percentualQuantidade = 0;
        if (totalItens == 2) {
            percentualQuantidade = 5;
        } else if (totalItens == 3) {
            percentualQuantidade = 7;
        } else if (totalItens >= 4) {
            percentualQuantidade = 10;
        }

        // Regra 2: Desconto adicional por categoria (por item)
        int percentualCategoria = 0;
        for (ItemCarrinho item : itens) {
            int descontoPorItem = getDescontoPorCategoria(item.getProduto().getCategoriaProduto());
            percentualCategoria += descontoPorItem * item.getQuantidade();
        }

        // Regra 3: Soma cumulativa com máximo de 25%
        int percentualTotal = percentualQuantidade + percentualCategoria;
        if (percentualTotal > 25) {
            percentualTotal = 25;
        }

        // Regra 4: Calcular desconto e total
        BigDecimal percentual = BigDecimal.valueOf(percentualTotal);
        BigDecimal valorDesconto = subtotal.multiply(percentual)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return new ResumoCarrinho(subtotal, valorDesconto);
    }

    private int getDescontoPorCategoria(CategoriaProduto categoria) {
        switch (categoria) {
            case CAPINHA:    return 3;
            case CARREGADOR: return 5;
            case FONE:       return 3;
            case PELICULA:   return 2;
            case SUPORTE:    return 2;
            default:         return 0;
        }
    }
}