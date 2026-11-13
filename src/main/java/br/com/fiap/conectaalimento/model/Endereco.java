package br.com.fiap.conectaalimento.model;

import br.com.fiap.conectaalimento.model.enums.Bairro;

/**
 * Objeto de Valor (Value Object - DDD): endereço de um usuário.
 * Não tem tabela própria; suas colunas ficam na tabela do usuário.
 */
public class Endereco {

    private static final double RAIO_TERRA_KM = 6371.0;

    private String logradouro;
    private String numero;
    private Bairro bairro;
    private String cep;
    private double latitude;
    private double longitude;

    /** Construtor padrão. */
    public Endereco() {
    }

    /** Construtor sem coordenadas: usa o centro do bairro como localização aproximada. */
    public Endereco(String logradouro, String numero, Bairro bairro, String cep) {
        this(logradouro, numero, bairro, cep, bairro.getLatitude(), bairro.getLongitude());
    }

    /** Construtor completo. */
    public Endereco(String logradouro, String numero, Bairro bairro, String cep, double latitude, double longitude) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.bairro = bairro;
        this.cep = cep;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** [POLIMORFISMO - overload] Distância (km) até outro endereço. */
    public double distanciaAte(Endereco destino) {
        return distanciaAte(destino.getLatitude(), destino.getLongitude());
    }

    /** [POLIMORFISMO - overload] Distância (km) até uma coordenada - fórmula de Haversine. */
    public double distanciaAte(double latitudeDestino, double longitudeDestino) {
        double dLat = Math.toRadians(latitudeDestino - latitude);
        double dLon = Math.toRadians(longitudeDestino - longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(latitudeDestino))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RAIO_TERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Endereço em uma linha (aparece no JSON como "enderecoCompleto"). */
    public String getEnderecoCompleto() {
        String cepFormatado = cep != null && cep.length() == 8 ? cep.substring(0, 5) + "-" + cep.substring(5) : cep;
        return logradouro + ", " + numero + " - " + (bairro == null ? "-" : bairro.getDescricao())
                + ", São Paulo/SP - CEP " + cepFormatado;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public void setBairro(Bairro bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
