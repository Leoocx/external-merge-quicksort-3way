import java.io.*;
import java.util.Random;

@SuppressWarnings("SpellCheckingInspection")
/**
 * Implementação do Algorítmo de Ordenação QuickSort Three-Way
 * Funcionamento geral:
 * 1. a classe QuickSortThreeWay receberá como argumentos arquivoEntrada.txt e arquivoSaida.txt
 * 2. os dados no arquivoEntrada.txt serão mapeados em um array
 * 3. o array local será ordenado utilizando o QuickSort Three-Way
 * 4. os dados do array já ordenado serão escritos no arquivoSaida.txt


 * Características do algorítimo:
 *  - é recursivo
 *  - utiliza a mesma estratégia do MergeSort de dividir para conquistar
 *  - não possui estabilidade
 *  - faz ordenação in-place
 *  - diferente do QuickSort convencional, evita reordenação de elementos iguais ao pivô


 * Complexidade de tempo:
 *  - O(nlog(n)) - melhor caso
 *  - O(n log(n)) - caso médio
 *  - O(n²) - pior caso

  **/
public class QuickSortThreeWay {

    /**
     * Implementação da classe principal: fase de ordenação
     **/

    // embaralha o array para evitar o pior caso O(n²)
    private static void embaralhar(int[] arr) {
        Random rand = new Random();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            swap(arr, i, j); // função auxiliar
        }
    }

    //aplicação do QuickSort Three-Way
    public static void quickSort3Way(int[] arr) {
        embaralhar(arr);
        sort(arr, 0, arr.length - 1);
    }

    // funçao principal de ordenação utilizando o Quick Sort Three-way recursivamente
    private static void sort(int[] arr, int low, int high) {
        if (low >= high) return; // caso base: subarray com 0 ou 1 elemento

        int lt = low;        // indice de elementos menores que pivô
        int gt = high;       // indice de elementos maiores que o pivô
        int i = low + 1;     // indice que percorre o array
        int pivot = arr[low]; // escolhe o pivô como primeiro elemento

        while (i <= gt) {
            if (arr[i] < pivot) {
                swap(arr, lt, i); // leva o elemento menor que o pivô para a esquerda
                lt++;
                i++;
            } else if (arr[i] > pivot) {
                swap(arr, i, gt); // leva o elemento maior que o pivô para a direita
                gt--;
            } else {
                i++; // elemento igual ao pivô, apenas avança
            }
        }

        // recursão para subarrays menores e maiores
        sort(arr, low, lt - 1);  // partição menor que o pivô
        sort(arr, gt + 1, high); // partição maior que o pivô
    }

    // função auxiliar responsável por trocar os elementos de lugar dentro do array
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * Implementação da classe principal: gerenciamento dos arquivos de entrada e saída
     **/

    // função responsável por ler o arquivo de entrada
    private static int[] lerNumerosDoArquivo(String caminho) throws IOException {

        BufferedReader leitor = new BufferedReader(new FileReader(caminho));
        String linha = leitor.readLine();
        leitor.close();

        if (linha == null || linha.trim().isEmpty()) {
            throw new IOException("arquivo vazio.");
        }

        String[] partes = linha.split(",\\s*");

        int[] numeros = new int[partes.length]; // array local que receberá os valores contidos no arquivo de entrada
        for (int i = 0; i < partes.length; i++) {
            numeros[i] = Integer.parseInt(partes[i]);
        }
        return numeros;
    }

    // função para escrever os números no arquivo de saída
    private static void escreverNumerosNoArquivo(int[] numeros, String caminho) throws IOException {

        BufferedWriter escritor = new BufferedWriter(new FileWriter(caminho));

        for (int i = 0; i < numeros.length; i++) {
            escritor.write(String.valueOf(numeros[i]));
            if (i < numeros.length - 1) {
                escritor.write(", ");
            }
        }
        escritor.newLine();
        escritor.close();
    }

    /**
     * Implementação da classe principal: função main responsável por ordenar os dados
     **/
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Argumentos inseridos de maneira errada.\n" +
                    "uso correto: java QuickSortThreeWay arquivoEntrada.txt arquivoSaida.txt");
            return;
        }
        //deverá ter o arquivo de entrada e o de saída como parâmetros
        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        try {
            // lê os números do arquivo de entrada
            int[] numeros = lerNumerosDoArquivo(arquivoEntrada);

            // ordena os números com QuickSort Three-way
            quickSort3Way(numeros);

            // mapeia os números ordenados para o arquivo de saída
            escreverNumerosNoArquivo(numeros, arquivoSaida);

            System.out.println("Ordenação concluída. Resultado salvo em: " + arquivoSaida);

        } catch (IOException e) {
            System.out.println("Erro ao acessar arquivos: " + e.getMessage());
        }
    }
}
    

