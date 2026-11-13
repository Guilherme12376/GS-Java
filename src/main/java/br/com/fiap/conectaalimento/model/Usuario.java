package br.com.fiap.conectaalimento.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Usuário da plataforma (abstrato).
 *
 * [HERANÇA] EstabelecimentoDoador e InstituicaoReceptora herdam os atributos,
 * os métodos (autenticar, atualizarContato, distanciaAte) e o construtor.
 */
public abstract class Usuario extends Entidade {

    private String nome;
    private String responsavel;
    private String email;
    private String senhaHash;
    private String telefone;
    private Endereco endereco;

    /** Construtor padrão. */
    public Usuario() {
        super();
    }

    /** Construtor não padrão (encadeado com Entidade via super). */
    public Usuario(Long id, String nome, String responsavel, String email, String senha, String telefone,
                   Endereco endereco) {
        super(id);
        this.nome = nome;
        this.responsavel = responsavel;
        this.email = email;
        this.senhaHash = senha == null ? null : gerarHash(senha);
        this.telefone = telefone;
        this.endereco = endereco;
    }

    /** Perfil do usuário. [POLIMORFISMO - override] implementado por cada subclasse. */
    public abstract String getTipoPerfil();

    /** Compara o hash da senha informada com o hash armazenado. */
    public boolean autenticar(String senhaInformada) {
        return senhaHash != null && senhaInformada != null && senhaHash.equals(gerarHash(senhaInformada));
    }

    /** [POLIMORFISMO - overload] Atualiza apenas o telefone. */
    public void atualizarContato(String novoTelefone) {
        this.telefone = novoTelefone;
    }

    /** [POLIMORFISMO - overload] Atualiza telefone e endereço. */
    public void atualizarContato(String novoTelefone, Endereco novoEndereco) {
        atualizarContato(novoTelefone);
        this.endereco = novoEndereco;
    }

    /** Distância aproximada (km) até outro usuário. */
    public double distanciaAte(Usuario outro) {
        return endereco.distanciaAte(outro.getEndereco());
    }

    /** Senha nunca é guardada em texto puro: apenas o hash SHA-256. */
    public void definirSenha(String senha) {
        this.senhaHash = gerarHash(senha);
    }

    public static String gerarHash(String texto) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /** Nunca aparece no JSON. */
    @JsonIgnore
    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
