import java.io.*;

/**
 * Classe auxiliar que gerencia a leitura bufferizada de um arquivo ordenado
 * durante a fase de merge, mantendo sempre a próxima linha disponível em cache.
 */
class BufferArquivoBinario {
    public static int TAMANHO_BUFFER = 2048; // Tamanho do buffer de leitura
    public BufferedReader leitor; // Stream de leitura do arquivo
    public File arquivoOriginal;  // Referência ao arquivo físico
    private String cache;         // Próxima linha a ser processada
    private boolean vazio;        // indica se chegou ao final

    public BufferArquivoBinario(File arquivo) throws IOException {
        this.arquivoOriginal = arquivo;
        this.leitor = new BufferedReader(new FileReader(arquivo), TAMANHO_BUFFER);
        this.recarregar(); // Carrega a primeira linha
    }

    public boolean vazio() {
        return vazio;
    }

    /**
     * Carrega a próxima linha do arquivo para o cache
     */
    private void recarregar() throws IOException {
        try {
            this.cache = leitor.readLine();
            this.vazio = (this.cache == null);
        } catch(EOFException oef) {
            this.vazio = true;
            this.cache = null;
        }
    }

    public void fechar() throws IOException {
        leitor.close();
    }

    /**
     * @return a próxima linha sem consumi-la (peek)
     */
    public String lerProxima() {
        return vazio ? null : cache;
    }

    /**
     * @return a próxima linha e já avança para a seguinte
     */
    public String remover() throws IOException {
        String resposta = lerProxima();
        recarregar(); // Pré-carrega a próxima linha
        return resposta;
    }
}