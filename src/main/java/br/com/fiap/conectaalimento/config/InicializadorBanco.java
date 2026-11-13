package br.com.fiap.conectaalimento.config;

import br.com.fiap.conectaalimento.dao.AlimentoDAO;
import br.com.fiap.conectaalimento.dao.EstabelecimentoDAO;
import br.com.fiap.conectaalimento.dao.InstituicaoDAO;
import br.com.fiap.conectaalimento.dao.RetiradaDAO;
import br.com.fiap.conectaalimento.dao.SolicitacaoDAO;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.AlimentoNaoPerecivel;
import br.com.fiap.conectaalimento.model.AlimentoPerecivel;
import br.com.fiap.conectaalimento.model.Endereco;
import br.com.fiap.conectaalimento.model.EstabelecimentoDoador;
import br.com.fiap.conectaalimento.model.InstituicaoReceptora;
import br.com.fiap.conectaalimento.model.Retirada;
import br.com.fiap.conectaalimento.model.Solicitacao;
import br.com.fiap.conectaalimento.model.enums.Bairro;
import br.com.fiap.conectaalimento.model.enums.CategoriaAlimento;
import br.com.fiap.conectaalimento.model.enums.CondicaoArmazenamento;
import br.com.fiap.conectaalimento.model.enums.StatusAlimento;
import br.com.fiap.conectaalimento.model.enums.StatusSolicitacao;
import br.com.fiap.conectaalimento.model.enums.TipoEstabelecimento;
import br.com.fiap.conectaalimento.model.enums.TipoInstituicao;
import br.com.fiap.conectaalimento.model.enums.UnidadeMedida;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ao subir a aplicação: cria as tabelas se ainda não existirem e, com o banco vazio,
 * carrega dados de demonstração (FICTÍCIOS) com datas relativas ao momento da execução.
 * Pode ser desligado com DB_INICIALIZAR=false / DB_DADOS_EXEMPLO=false.
 */
@Component
public class InicializadorBanco implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorBanco.class);
    private static final String SENHA_DEMO = "123456";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final EstabelecimentoDAO estabelecimentoDAO;
    private final InstituicaoDAO instituicaoDAO;
    private final AlimentoDAO alimentoDAO;
    private final SolicitacaoDAO solicitacaoDAO;
    private final RetiradaDAO retiradaDAO;

    @Value("${app.db.inicializar:true}")
    private boolean inicializar;

    @Value("${app.db.dados-exemplo:true}")
    private boolean dadosExemplo;

    @Value("${app.db.script-schema}")
    private Resource scriptSchema;

    public InicializadorBanco(DataSource dataSource, JdbcTemplate jdbcTemplate, EstabelecimentoDAO estabelecimentoDAO,
                              InstituicaoDAO instituicaoDAO, AlimentoDAO alimentoDAO, SolicitacaoDAO solicitacaoDAO,
                              RetiradaDAO retiradaDAO) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.estabelecimentoDAO = estabelecimentoDAO;
        this.instituicaoDAO = instituicaoDAO;
        this.alimentoDAO = alimentoDAO;
        this.solicitacaoDAO = solicitacaoDAO;
        this.retiradaDAO = retiradaDAO;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!inicializar) {
            return;
        }
        if (!tabelasExistem()) {
            LOG.info("Criando as tabelas do Conecta Alimento ({})", scriptSchema.getFilename());
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(scriptSchema);
            populator.setContinueOnError(true);
            populator.execute(dataSource);
        }
        if (dadosExemplo && estabelecimentoDAO.contar() == 0) {
            LOG.info("Banco vazio: carregando dados de demonstração");
            carregarDadosExemplo();
        }
    }

    private boolean tabelasExistem() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM T_CA_RETIRADA", Long.class);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void carregarDadosExemplo() {
        LocalDateTime agora = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime inicioDaHora = agora.withMinute(0);
        LocalDate hoje = agora.toLocalDate();

        EstabelecimentoDoador padaria = salvar(new EstabelecimentoDoador(null, "Padaria Santos", "Marcelo Santos",
                "padaria@demo.com", SENHA_DEMO, "11987654321",
                new Endereco("Rua Hannemann", "300", Bairro.PARI, "03031000", -23.5300, -46.6195),
                "12345678000190", TipoEstabelecimento.PADARIA), agora);
        EstabelecimentoDoador mercado = salvar(new EstabelecimentoDoador(null, "Mercado Bom Preço", "Ana Lima",
                "mercado@demo.com", SENHA_DEMO, "11933332222",
                new Endereco("Rua Oriente", "500", Bairro.BRAS, "03016000", -23.5420, -46.6185),
                "23456789000101", TipoEstabelecimento.MERCADO), agora);
        EstabelecimentoDoador hortifruti = salvar(new EstabelecimentoDoador(null, "Hortifruti Ribeiro", "Jorge Okada",
                "hortifruti@demo.com", SENHA_DEMO, "11944445555",
                new Endereco("Rua Ribeiro de Lima", "200", Bairro.BOM_RETIRO, "01122000", -23.5285, -46.6385),
                "34567890000112", TipoEstabelecimento.HORTIFRUTI), agora);

        InstituicaoReceptora cozinha = new InstituicaoReceptora(null, "Cozinha Comunitária Bom Retiro", "Helena Souza",
                "cozinha@demo.com", SENHA_DEMO, "11912345678",
                new Endereco("Rua Prates", "400", Bairro.BOM_RETIRO, "01121000", -23.5250, -46.6365),
                TipoInstituicao.COZINHA_COMUNITARIA, 120);
        cozinha.setDataCadastro(agora.minusDays(30));
        instituicaoDAO.inserir(cozinha);
        InstituicaoReceptora ong = new InstituicaoReceptora(null, "ONG Prato Solidário", "Carlos Pereira",
                "ong@demo.com", SENHA_DEMO, "11955554444",
                new Endereco("Rua do Gasômetro", "300", Bairro.BRAS, "03004000", -23.5455, -46.6230),
                TipoInstituicao.ONG, 80);
        ong.setDataCadastro(agora.minusDays(30));
        instituicaoDAO.inserir(ong);

        // ----- alimentos disponíveis agora
        Alimento paes = publicar(new AlimentoPerecivel(null, "Pães franceses do dia", CategoriaAlimento.PANIFICACAO, 8,
                UnidadeMedida.KG, hoje.plusDays(1), inicioDaHora, agora.plusHours(6), padaria,
                CondicaoArmazenamento.TEMPERATURA_AMBIENTE), "Retirar no balcão dos fundos. Falar com o Marcelo.",
                agora.minusMinutes(50));
        Alimento salgados = publicar(new AlimentoPerecivel(null, "Salgados assados (esfiha e enroladinho)",
                CategoriaAlimento.PANIFICACAO, 40, UnidadeMedida.UNIDADE, hoje.plusDays(1), inicioDaHora,
                agora.plusHours(6), padaria, CondicaoArmazenamento.TEMPERATURA_AMBIENTE), null, agora.minusMinutes(45));
        publicar(new AlimentoNaoPerecivel(null, "Arroz tipo 1 - pacotes de 5 kg", CategoriaAlimento.MERCEARIA, 10,
                UnidadeMedida.PACOTE, hoje.plusDays(120), inicioDaHora, agora.plusDays(3), mercado, true, "L2309"),
                "Rótulo com pequena avaria; embalagem interna intacta.", agora.minusHours(3));
        Alimento iogurte = publicar(new AlimentoPerecivel(null, "Iogurte natural 170 g", CategoriaAlimento.LATICINIOS,
                24, UnidadeMedida.UNIDADE, hoje.plusDays(3), inicioDaHora, agora.plusDays(2), mercado,
                CondicaoArmazenamento.REFRIGERADO), "Levar caixa térmica.", agora.minusHours(2));
        publicar(new AlimentoPerecivel(null, "Bananas maduras", CategoriaAlimento.HORTIFRUTI, 15, UnidadeMedida.KG,
                hoje.plusDays(2), inicioDaHora, agora.plusDays(1), hortifruti,
                CondicaoArmazenamento.TEMPERATURA_AMBIENTE), null, agora.minusHours(1));
        publicar(new AlimentoPerecivel(null, "Legumes variados (cenoura, abobrinha, chuchu)",
                CategoriaAlimento.HORTIFRUTI, 12, UnidadeMedida.KG, hoje.plusDays(4), inicioDaHora, agora.plusDays(2),
                hortifruti, CondicaoArmazenamento.REFRIGERADO), null, agora.minusMinutes(30));

        // ----- solicitações pendentes
        solicitar(salgados, ong, 20, agora.minusMinutes(20));
        solicitar(iogurte, ong, 24, agora.minusMinutes(90));
        if (paes.getId() == null) {
            throw new IllegalStateException("Falha ao carregar dados de demonstração");
        }

        // ----- histórico (doações concluídas)
        concluida(new AlimentoPerecivel(null, "Pães de forma", CategoriaAlimento.PANIFICACAO, 6, UnidadeMedida.KG,
                        hoje.minusDays(2), null, null, padaria, CondicaoArmazenamento.TEMPERATURA_AMBIENTE),
                cozinha, hoje.minusDays(3).atTime(17, 0), hoje.minusDays(3).atTime(19, 30));
        concluida(new AlimentoNaoPerecivel(null, "Feijão carioca 1 kg", CategoriaAlimento.MERCEARIA, 20,
                        UnidadeMedida.PACOTE, hoje.plusDays(90), null, null, mercado, true, "F881"),
                ong, hoje.minusDays(5).atTime(9, 0), hoje.minusDays(4).atTime(10, 0));
        concluida(new AlimentoPerecivel(null, "Tomates", CategoriaAlimento.HORTIFRUTI, 10, UnidadeMedida.KG,
                        hoje.minusDays(1), null, null, hortifruti, CondicaoArmazenamento.TEMPERATURA_AMBIENTE),
                cozinha, hoje.minusDays(2).atTime(8, 0), hoje.minusDays(2).atTime(11, 30));
        concluida(new AlimentoPerecivel(null, "Pães doces e bolos", CategoriaAlimento.PANIFICACAO, 4, UnidadeMedida.KG,
                        hoje, null, null, padaria, CondicaoArmazenamento.TEMPERATURA_AMBIENTE),
                ong, hoje.minusDays(1).atTime(16, 0), hoje.minusDays(1).atTime(18, 0));

        // ----- oferta que expirou sem retirada
        Alimento sanduiches = new AlimentoPerecivel(null, "Sanduíches naturais", CategoriaAlimento.REFEICAO_PRONTA, 10,
                UnidadeMedida.UNIDADE, hoje.minusDays(4), hoje.minusDays(4).atTime(14, 0),
                hoje.minusDays(4).atTime(20, 0), padaria, CondicaoArmazenamento.REFRIGERADO);
        sanduiches.setStatus(StatusAlimento.EXPIRADO);
        publicar(sanduiches, null, hoje.minusDays(4).atTime(13, 0));
    }

    private EstabelecimentoDoador salvar(EstabelecimentoDoador doador, LocalDateTime agora) {
        doador.setDataCadastro(agora.minusDays(30));
        return estabelecimentoDAO.inserir(doador);
    }

    private Alimento publicar(Alimento alimento, String observacoes, LocalDateTime cadastradoEm) {
        alimento.setObservacoes(observacoes);
        alimento.setDeclaracaoSeguranca(true);
        alimento.setDataCadastro(cadastradoEm);
        return alimentoDAO.inserir(alimento);
    }

    private void solicitar(Alimento alimento, InstituicaoReceptora instituicao, double quantidade,
                           LocalDateTime quando) {
        Solicitacao s = new Solicitacao(null, alimento, instituicao, quantidade);
        s.setDataCadastro(quando);
        solicitacaoDAO.inserir(s);
        alimentoDAO.atualizarStatus(alimento.getId(), StatusAlimento.SOLICITADO);
    }

    private void concluida(Alimento alimento, InstituicaoReceptora instituicao, LocalDateTime cadastradoEm,
                           LocalDateTime retiradoEm) {
        alimento.setInicioRetirada(cadastradoEm);
        alimento.setFimRetirada(retiradoEm.plusHours(2));
        alimento.setStatus(StatusAlimento.CONCLUIDO);
        publicar(alimento, null, cadastradoEm);

        Solicitacao s = new Solicitacao(null, alimento, instituicao, alimento.getQuantidade());
        s.setStatus(StatusSolicitacao.CONCLUIDA);
        s.setDataCadastro(cadastradoEm.plusMinutes(20));
        s.setDataResposta(cadastradoEm.plusMinutes(35));
        solicitacaoDAO.inserir(s);

        Retirada r = new Retirada(null, s, retiradoEm);
        r.setDataCadastro(cadastradoEm.plusMinutes(40));
        r.setEntregaConfirmada(true);
        r.setDataEntrega(retiradoEm);
        r.setQuantidadeEntregue(alimento.getQuantidade());
        r.setRecebimentoConfirmado(true);
        r.setDataRecebimento(retiradoEm.plusMinutes(10));
        r.setQuantidadeRecebida(alimento.getQuantidade());
        retiradaDAO.inserir(r);
    }
}
