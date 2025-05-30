import java.util.*;
import java.io.*;

/**
 * Implementação de um algoritmo de ordenação externa (external sort) para arquivos grandes.
 *
 * Funcionamento geral:
 * 1. Divide o arquivo grande em blocos que cabem na memória
 * 2. Ordena cada bloco individualmente e salva em arquivos temporários
 * 3. Mescla todos os arquivos temporários ordenados em um único arquivo de saída
 *
 * Complexidade: O(n log n) no caso médio, com O(n/M) passes sobre os dados,
 * onde n é o número de elementos e M é o número de elementos que cabem na memória
 */
public class ExternalSort {

    /**
     * Calcula o tamanho ideal dos blocos para divisão do arquivo.
     * Tomando cuidado em:
     * - Não usar muita memória (evitar exceção OutOfMemoryError)
     * - Não criar muitos arquivos temporários (evitar atingir limite do sistema)
     *
     * @param arquivoParaOrdenar arquivo que será ordenado
     * @return tamanho ideal do bloco em bytes
     */
    public static long estimarMelhorTamanhoDeBlocos(File arquivoParaOrdenar) {
        long tamanhoDoArquivo = arquivoParaOrdenar.length();
        // Limite prático de arquivos abertos simultaneamente
        final int MAXIMO_ARQUIVOS_TEMP = 4096;
        long tamanhoBloco = tamanhoDoArquivo / MAXIMO_ARQUIVOS_TEMP;

        // Ajusta com base na memória disponível
        long memoriaLivre = Runtime.getRuntime().freeMemory();
        if(tamanhoBloco < memoriaLivre/2)
            tamanhoBloco = memoriaLivre/2; // Usa mais memória se disponível
        else if(tamanhoBloco >= memoriaLivre)
            System.err.println("Aviso: Pode ficar sem memória durante a execução.");

        return tamanhoBloco;
    }

    /**
     * Fase de divisão e ordenação inicial:
     * 1. Lê o arquivo em blocos que cabem na memória
     * 2. Ordena cada bloco em memória primária
     * 3. Salva cada bloco ordenado em arquivo temporário
     *
     * @param arquivo arquivo de entrada a ser ordenado
     * @param comparador implementação de Comparator para definir a ordem
     * @return lista de arquivos temporários ordenados
     */
    public static List<File> ordenarEmLotes(File arquivo, Comparator<String> comparador) throws IOException {
        List<File> arquivos = new ArrayList<File>();
        BufferedReader leitor = new BufferedReader(new FileReader(arquivo));
        long tamanhoBloco = estimarMelhorTamanhoDeBlocos(arquivo);

        try {
            List<String> listaTemporaria = new ArrayList<String>();
            String linha = "";

            try {
                while(linha != null) {
                    long tamanhoBlocoAtual = 0;
                    // Lê linhas até preencher o bloco ou acabar o arquivo
                    while((tamanhoBlocoAtual < tamanhoBloco) && ((linha = leitor.readLine()) != null)) {
                        listaTemporaria.add(linha);
                        // Estimativa de consumo de memória (2 bytes por char + overhead)
                        tamanhoBlocoAtual += linha.length() * 2 + 40;
                    }

                    // Ordena e salva o bloco completo
                    if(!listaTemporaria.isEmpty()) {
                        arquivos.add(ordenarESalvar(listaTemporaria, comparador));
                        listaTemporaria.clear();
                    }
                }
            } catch(EOFException oef) {
                // Trata o final do arquivo inesperado
                if(!listaTemporaria.isEmpty()) {
                    arquivos.add(ordenarESalvar(listaTemporaria, comparador));
                    listaTemporaria.clear();
                }
            }
        } finally {
            leitor.close();
        }
        return arquivos;
    }

    /**
     * Método auxiliar que ordena um bloco na memória e salva em arquivo temporário
     *
     * @param listaTemporaria lista de strings a serem ordenadas
     * @param comparador critério de ordenação
     * @return arquivo temporário com os dados ordenados
     */
    public static File ordenarESalvar(List<String> listaTemporaria, Comparator<String> comparador) throws IOException {
        // Ordena usando merge sort
        mergeSort(listaTemporaria, comparador);

        // Restante do método permanece igual
        File novoArquivoTemp = File.createTempFile("ordenarEmLotes", "arquivoPlano");
        novoArquivoTemp.deleteOnExit();

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(novoArquivoTemp))) {
            for(String linha : listaTemporaria) {
                escritor.write(linha);
                escritor.newLine();
            }
        }
        return novoArquivoTemp;
    }

    // Implementação do Merge Sort
    private static void mergeSort(List<String> lista, Comparator<String> comparador) {
        if (lista.size() <= 1) return;

        // Divide a lista em duas metades
        int meio = lista.size() / 2;
        List<String> esquerda = new ArrayList<>(lista.subList(0, meio));
        List<String> direita = new ArrayList<>(lista.subList(meio, lista.size()));

        // Ordena recursivamente cada metade
        mergeSort(esquerda, comparador);
        mergeSort(direita, comparador);

        // Merge das duas metades ordenadas
        merge(lista, esquerda, direita, comparador);
    }

    private static void merge(List<String> resultado, List<String> esquerda,
                              List<String> direita, Comparator<String> comparador) {
        int i = 0, j = 0, k = 0;

        while (i < esquerda.size() && j < direita.size()) {
            if (comparador.compare(esquerda.get(i), direita.get(j)) <= 0) {
                resultado.set(k++, esquerda.get(i++));
            } else {
                resultado.set(k++, direita.get(j++));
            }
        }

        // Copia elementos restantes
        while (i < esquerda.size()) {
            resultado.set(k++, esquerda.get(i++));
        }

        while (j < direita.size()) {
            resultado.set(k++, direita.get(j++));
        }
    }

    /**
     * Fase de merge (mesclagem) dos arquivos ordenados:
     * Usa um heap (fila de prioridade) para eficientemente mesclar múltiplos
     * arquivos ordenados em um único arquivo de saída.
     *
     * @param arquivos lista de arquivos temporários ordenados
     * @param arquivoSaida arquivo de saída final ordenado
     * @param comparador critério de ordenação
     * @return número total de linhas processadas
     */
    public static int mesclarArquivosOrdenados(List<File> arquivos, File arquivoSaida,
                                               final Comparator<String> comparador) throws IOException {

        // Cria um heap mínimo para eficientemente obter o próximo menor elemento
        PriorityQueue<BufferArquivoBinario> filaPrioridade = new PriorityQueue<>(11,
                new Comparator<BufferArquivoBinario>() {
                    public int compare(BufferArquivoBinario i, BufferArquivoBinario j) {
                        return comparador.compare(i.lerProxima(), j.lerProxima());
                    }
                });

        // Inicializa o heap com um buffer para cada arquivo
        for (File arquivo : arquivos) {
            BufferArquivoBinario buffer = new BufferArquivoBinario(arquivo);
            if(!buffer.vazio()) {
                filaPrioridade.add(buffer);
            }
        }

        // Escreve o resultado ordenado no arquivo de saída
        BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivoSaida));
        int contadorLinhas = 0;

        try {
            // Processa enquanto houver elementos no heap
            while(!filaPrioridade.isEmpty()) {
                BufferArquivoBinario buffer = filaPrioridade.poll();
                String linha = buffer.remover();
                escritor.write(linha);
                escritor.newLine();
                contadorLinhas++;

                // Se o buffer ainda tem elementos, recoloca no heap
                if(!buffer.vazio()) {
                    filaPrioridade.add(buffer);
                } else {
                    // Se o buffer esvaziou, fecha e deleta o arquivo
                    buffer.fechar();
                    buffer.arquivoOriginal.delete();
                }
            }
        } finally {
            escritor.close();
            // Garante que todos os buffers sejam fechados
            for(BufferArquivoBinario buffer : filaPrioridade) {
                buffer.fechar();
            }
        }
        return contadorLinhas;
    }

    public static void main(String[] args) throws IOException {
        if(args.length<2) {
            System.out.println("por favor forneça os nomes dos arquivos de entrada e saída");
            return;
        }
        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        // Comparador modificado para ordenar pelo ID numérico
        Comparator<String> comparador = new Comparator<String>() {
            public int compare(String linha1, String linha2) {
                // Extrai o ID (primeiro campo antes da vírgula)
                int id1 = Integer.parseInt(linha1.split(",")[0]);
                int id2 = Integer.parseInt(linha2.split(",")[0]);
                return Integer.compare(id1, id2);
            }};

        List<File> arquivosTemp = ordenarEmLotes(new File(arquivoEntrada), comparador);
        mesclarArquivosOrdenados(arquivosTemp, new File(arquivoSaida), comparador);
    }
}