package br.com.fiap.conectaalimento.dto;

/** Resposta do login: quem entrou e com qual perfil (o front-end usa o id nas demais chamadas). */
public record LoginResponse(Long id, String nome, String responsavel, String email, String perfil) {
}
