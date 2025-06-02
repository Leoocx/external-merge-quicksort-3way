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
    private static long totalMemoriaUtilizada = 0;    // Toda a memoria utilizada pelo programa será incrementada nessa variavel
    private static long tempoInicio;                  // O inicio do tempo de execução
    private static long tempoFim;                     // O final do tempo de execução

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
        // Configurações para economia de memória
        final int MAXIMO_ARQUIVOS_TEMP = 2048; // Reduz arquivos para limitar overhead
        final long TAMANHO_MINIMO_BLOCO = 8 * 1024 * 1024;  // 8MB (balanceia I/O e RAM)
        final long TAMANHO_MAXIMO_BLOCO = 50 * 1024 * 1024; // 50MB (evita picos de RAM)
        final double PORCENTAGEM_MEMORIA = 0.3; // Usa apenas 30% da memória livre

        long tamanhoBloco = arquivoParaOrdenar.length() / MAXIMO_ARQUIVOS_TEMP;
        long memoriaDisponivel = (long) (Runtime.getRuntime().freeMemory() * PORCENTAGEM_MEMORIA);

        // Ajuste com limites rigorosos
        tamanhoBloco = Math.max(TAMANHO_MINIMO_BLOCO,
                Math.min(TAMANHO_MAXIMO_BLOCO,
                        Math.min(tamanhoBloco, memoriaDisponivel)));

        if (tamanhoBloco > memoriaDisponivel) {
            System.err.printf("[OTIMIZADO] Memória conservativa: Bloco reduzido para %.2fMB (%.2fMB solicitado)%n",
                    memoriaDisponivel / (1024.0 * 1024),
                    tamanhoBloco / (1024.0 * 1024));
            tamanhoBloco = memoriaDisponivel;
        }

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
            String linha = leitor.readLine();
            if (linha == null) return arquivos;

            String[] numeros = linha.split(", ");
            long tamanhoBlocoAtual = 0;

            for (String numero : numeros) {
                listaTemporaria.add(numero);
                tamanhoBlocoAtual += numero.length() * 2 + 40;

                if (tamanhoBlocoAtual >= tamanhoBloco) {
                    // Medir memória antes e depois de ordenar
                    long memoriaAntes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    File arquivoTemp = ordenarESalvar(listaTemporaria, comparador);
                    long memoriaDepois = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    totalMemoriaUtilizada += (memoriaDepois - memoriaAntes);

                    arquivos.add(arquivoTemp);
                    listaTemporaria.clear();
                    tamanhoBlocoAtual = 0;
                }
            }

            if (!listaTemporaria.isEmpty()) {
                long memoriaAntes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                File arquivoTemp = ordenarESalvar(listaTemporaria, comparador);
                long memoriaDepois = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                totalMemoriaUtilizada += (memoriaDepois - memoriaAntes);

                arquivos.add(arquivoTemp);
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
        // Ordena a lista usando o algoritmo merge sort
        mergeSort(listaTemporaria, comparador);

        // Cria um arquivo temporário que será automaticamente deletado quando sair da JVM
        File novoArquivoTemp = File.createTempFile("ordenarEmLotes", "arquivoPlano");
        novoArquivoTemp.deleteOnExit();

        // Escreve os dados ordenados no arquivo temporário
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(novoArquivoTemp))) {
            for(String linha : listaTemporaria) {
                escritor.write(linha);  // Escreve cada elemento ordenado
                escritor.newLine();
            }
        }
        return novoArquivoTemp;  // Retorna o arquivo temporário que foi criado
    }

    /**
     * Implementação do algoritmo Merge Sort para ordenar a lista
     *
     * @param lista Lista a ser ordenada
     * @param comparador Critério de ordenação
     */
    private static void mergeSort(List<String> lista, Comparator<String> comparador) {
        // Caso base: lista com 0 ou 1 elemento já está ordenada
        if (lista.size() <= 1) return;

        // Divide a lista em duas partes
        int meio = lista.size() / 2;
        List<String> esquerda = new ArrayList<>(lista.subList(0, meio));           // Sublista esquerda
        List<String> direita = new ArrayList<>(lista.subList(meio, lista.size())); // Sublista direita

        // Ordena recursivamente cada metade
        mergeSort(esquerda, comparador);
        mergeSort(direita, comparador);

        // Combina as duas metades ordenadas
        merge(lista, esquerda, direita, comparador);
    }

    /**
     * Combina duas listas ordenadas em uma única lista ordenada
     *
     * @param resultado Lista que receberá o resultado combinado
     * @param esquerda Lista ordenada (metade esquerda)
     * @param direita Lista ordenada (metade direita)
     * @param comparador Critério de ordenação
     */
    private static void merge(List<String> resultado, List<String> esquerda,
                              List<String> direita, Comparator<String> comparador) {
        int i = 0, j = 0, k = 0;  // Índices para esquerda, direita e resultado

        // Combina enquanto houver elementos em ambas as listas
        while (i < esquerda.size() && j < direita.size()) {
            // Seleciona o menor elemento entre as duas listas
            // Condição do if usando compare, exemplos:
            //  Integer.compare(2, 3)  // Retorna -1 (2 < 3)
            //  Integer.compare(3, 3)  // Retorna 0  (3 == 3)
            //  Integer.compare(4, 3)  // Retorna 1  (4 > 3)
            if (comparador.compare(esquerda.get(i), direita.get(j)) <= 0) {
                resultado.set(k++, esquerda.get(i++));  // Toma da esquerda
            } else {
                resultado.set(k++, direita.get(j++));   // Toma da direita
            }
        }

        // Adiciona os elementos restantes da esquerda (se tiver)
        while (i < esquerda.size()) {
            resultado.set(k++, esquerda.get(i++));
        }

        // Adiciona os elementos restantes da direita (se tiver)
        while (j < direita.size()) {
            resultado.set(k++, direita.get(j++));
        }
    }

    /**
     * Fase de merge (mesclagem) dos arquivos ordenados:
     * Usa um heap (fila de prioridade) para mesclar de forma mais eficiente vários
     * arquivos ordenados em um único arquivo de saída.
     *
     * @param arquivos lista de arquivos temporários ordenados
     * @param arquivoSaida arquivo de saída final ordenado
     * @param comparador critério de ordenação
     * @return número total de linhas processadas
     */
    public static int mesclarArquivosOrdenados(List<File> arquivos, File arquivoSaida,
                                               final Comparator<String> comparador) throws IOException {

        /**
         * Cria uma fila de prioridade (min-heap) para uma mesclagem mais eficiente
         * PriorityQueue: Estrutura que sempre retorna o menor elemento (min-heap).
         * Ex: Se tivermos [3,1,2], o metodo poll() remove e retorna 1 primeiro.
         */
        PriorityQueue<BufferArquivoBinario> filaPrioridade = new PriorityQueue<>(11,
                new Comparator<BufferArquivoBinario>() {
                    // Comparador personalizado para ordenar os buffers pelos seus próximos elementos
                    public int compare(BufferArquivoBinario i, BufferArquivoBinario j) {
                        return comparador.compare(i.lerProxima(), j.lerProxima());
                    }
                });

        // Medição inicial de memória utilizada antes do processo de mesclagem
        long memoriaAntesMesclagem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Adiciona cada arquivo temporário à fila de prioridade
        for (File arquivo : arquivos) {
            // Cria um buffer para ler o arquivo
            BufferArquivoBinario buffer = new BufferArquivoBinario(arquivo);
            // Só adiciona à fila se o arquivo não estiver vazio
            if(!buffer.vazio()) {
                filaPrioridade.add(buffer);
            }
        }

        // Prepara o escritor para o arquivo de saída final
        BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivoSaida));
        int contadorLinhas = 0;          // Contador de linhas processadas
        boolean primeiroElemento = true; // para controlar a formatação da saída

        try {
            // Processa enquanto houver elementos na fila de prioridade
            while(!filaPrioridade.isEmpty()) {
                // Remove o buffer com o menor elemento atual
                BufferArquivoBinario buffer = filaPrioridade.poll();
                // Obtém o próximo elemento ordenado
                String linha = buffer.remover();

                // Formatação: adiciona vírgula antes de todos os elementos, exceto o primeiro
                if (!primeiroElemento) {
                    escritor.write(", ");
                } else {
                    primeiroElemento = false;
                }

                // Escreve o elemento no arquivo de saída (removendo espaços em branco)
                escritor.write(linha.trim());
                contadorLinhas++;

                // Se o buffer ainda tem elementos, recoloca na fila
                if(!buffer.vazio()) {
                    filaPrioridade.add(buffer);
                } else {
                    // Se o buffer esvaziou, fecha e deleta o arquivo temporário
                    buffer.fechar();
                    buffer.arquivoOriginal.delete();
                }
            }
        } finally {
            // Garante que o escritor seja fechado
            escritor.close();
            // Fecha todos os buffers restantes na fila
            for(BufferArquivoBinario buffer : filaPrioridade) {
                buffer.fechar();
            }
        }

        // Calcula a memória utilizada durante a mesclagem e atualiza o total
        long memoriaDepoisMesclagem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        totalMemoriaUtilizada += (memoriaDepoisMesclagem - memoriaAntesMesclagem);

        // Retorna o total de linhas processadas
        return contadorLinhas;
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 2) {
            System.out.println("Forma de utilizar: java ExternalSort arquivoEntrada.txt arquivoSaida.txt");
            return;
        }
        String arquivoEntrada = args[0];    // Primeiro argumento recebido pelo terminal
        String arquivoSaida = args[1];      // Segundo argumento recebido pelo terminal

        // inicio da contagem de tempo
        tempoInicio = System.currentTimeMillis();

        /**
         * Como os dados são lidos como texto, é mais vantajoso trabalhar diretamente com Strings para evitar
         * conversões desnecessarias e ser melhor de se manipular, por isso,
         * foi utilizado Comparator<String> e não Comparator<Integer>
        *
         * */
        Comparator<String> comparador = new Comparator<String>() {
            public int compare(String num1, String num2) {
                return Integer.compare(Integer.parseInt(num1), Integer.parseInt(num2));
            }
        };

        List<File> arquivosTemp = ordenarEmLotes(new File(arquivoEntrada), comparador);
        mesclarArquivosOrdenados(arquivosTemp, new File(arquivoSaida), comparador);

        // fim da contagem de tempo
        tempoFim = System.currentTimeMillis();
        long tempoTotal = tempoFim - tempoInicio;


        System.out.println("\nEstatísticas da Ordenação:");
        System.out.println("-------------------------");
        System.out.println("Tempo total de execução: " + tempoTotal + " ms");
        System.out.println("Memória total utilizada: " + (totalMemoriaUtilizada / (1024 * 1024)) + " MB");
        System.out.println("Número de arquivos temporários criados: " + arquivosTemp.size());

        // apagar os arquivos temporários
        for (File tempFile : arquivosTemp) {
            tempFile.delete();
        }
    }
}