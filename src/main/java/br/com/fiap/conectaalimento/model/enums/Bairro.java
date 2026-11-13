package br.com.fiap.conectaalimento.model.enums;

/**
 * Bairros do projeto-piloto (eixo Bom Retiro - Pari - Brás, centro de São Paulo).
 * As coordenadas aproximadas (centro do bairro) são usadas quando o endereço
 * não informa latitude/longitude, simulando um serviço de geocodificação.
 */
public enum Bairro implements Descritivel {

    BOM_RETIRO("Bom Retiro", -23.5265, -46.6413),
    PARI("Pari", -23.5285, -46.6170),
    BRAS("Brás", -23.5435, -46.6160);

    private final String descricao;
    private final double latitude;
    private final double longitude;

    Bairro(String descricao, double latitude, double longitude) {
        this.descricao = descricao;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
