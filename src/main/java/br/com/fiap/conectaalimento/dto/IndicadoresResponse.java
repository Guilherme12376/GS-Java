package br.com.fiap.conectaalimento.dto;

import java.util.Map;

/** Painel de impacto do piloto (indicadores propostos no projeto). */
public record IndicadoresResponse(
        long doacoesConcluidas,
        double kgDoados,
        Map<String, Double> quantidadeDoadaPorUnidade,
        long estabelecimentosParticipantes,
        long estabelecimentosQueJaDoaram,
        long instituicoesCadastradas,
        long instituicoesAtendidas,
        long alimentosDisponiveisAgora,
        double percentualDoacoesRetiradas,
        double tempoMedioCadastroAteRetiradaHoras,
        long totalSolicitacoes,
        long solicitacoesAceitasOuConcluidas,
        long solicitacoesRecusadas,
        long solicitacoesNaoAtendidasOuCanceladas,
        long solicitacoesPendentes) {
}
