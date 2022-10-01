package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Integer.*;
import static java.util.Arrays.*;
public class Main {
    public static void main(String[] args) {
        int MAX = Integer.MAX_VALUE / 2;
        int vNum = 6;
        int eNum = 0;
        int[][] edges = new int[2][vNum * vNum];
        int[][] subgraph = new int[vNum][vNum];
        int[][] graph =
                {
                        {MAX, 1, 1, MAX, MAX, 1},
                        {1, MAX, 1, MAX, MAX, MAX},
                        {1, 1, MAX, 1, MAX, 1},
                        {MAX, MAX, 1, MAX, 1, 1},
                        {MAX, MAX, MAX, 1, MAX, 1},
                        {1, MAX, 1, 1, 1, MAX},
                };
        System.out.format("%1s%32s%40s", "Полный перебор", "Имитационное моделирование", "Ускоренное имитационное моделирование");
                System.out.println();
        ArrayList<Double> bruteForce = new ArrayList<>();
        for (double p = 0; p <= 1; p += 0.1) {
            eNum = 0;
            double prob2 = 0;
            int k = 0;
            for (int i = 0; i < vNum; i++) {
                for (int j = 0; j < vNum; j++) {
                    if (i == j) {
                        break;
                    }
                    if (graph[i][j] == 1) {
                        eNum++;
                        edges[0][k] = i;
                        edges[1][k] = j;
                        k++;
                    }
                }
            }
            int[] check = new int[eNum];
            int clearCount = 0;
            for (int i = 0; i < eNum; i++) {
                check[i] = 1;
            }
            clearSubgraph(subgraph, vNum, MAX);
            for (int i = 0; i < eNum; i++) {
                if (check[i] == 2) {
                    clearCount++;
                    subgraph[edges[0][i]][edges[1][i]] = 1;
                    subgraph[edges[1][i]][edges[0][i]] = 1;
                }
            }
            if (dijkstra(0, vNum, subgraph, MAX) < MAX) {
                prob2 += pow(p, clearCount) * pow(1 - p, eNum - clearCount);
            }
            while (enumeration(check, 2, eNum)) {
                clearSubgraph(subgraph, vNum, MAX);
                clearCount = 0;
                for (int i = 0; i < eNum; i++) {
                    if (check[i] == 2) {
                        clearCount++;
                        subgraph[edges[0][i]][edges[1][i]] = 1;
                        subgraph[edges[1][i]][edges[0][i]] = 1;
                    }
                }
                if (dijkstra(0, vNum, subgraph, MAX) < MAX) {
                    prob2 += pow(p, clearCount) * pow(1 - p, eNum - clearCount);
                }
            }
            bruteForce.add(prob2);
        }
        save(bruteForce, "bruteForce.txt");
        double e = 0.01;
        double N = 9 / (e * e * 4);
        ArrayList<Double> imitationModeling = new ArrayList<>();
        for (double p = 0; p <= 1; p += 0.1) {
            double s = 0;
            for (int i = 0; i < N; i++) {
                int[] y = new int[eNum];
                int W = 0;
                for (int j = 0; j < eNum; j++) {
                    y[j] = randomP(p);
                    W += y[j];
                }
                clearSubgraph(subgraph, vNum, MAX);
                for (int j = 0; j < W; j++) {
                    int r = randomEdges(eNum);
                    if (subgraph[edges[0][r]][edges[1][r]] != 1) {
                        subgraph[edges[0][r]][edges[1][r]] = 1;
                        subgraph[edges[1][r]][edges[0][r]] = 1;
                    } else {
                        j--;
                    }
                }
                if (dijkstra(0, vNum, subgraph, MAX) < MAX) {
                    s += 1;
                }
            }
            double pIm = s / N;
            imitationModeling.add(pIm);
        }
        save(imitationModeling, "imitationModeling.txt");
        int lMin = 2;
        int lMax = eNum - 2;
        ArrayList<Double> imitationFastModeling = new ArrayList<>();
        ArrayList<Double> advantage = new ArrayList<>();
        for (double p = 0; p <= 1; p += 0.1) {
            double NFast = 0;
            double s = 0;
            for (int i = 0; i < N; i++) {
                int[] y = new int[eNum];
                int W = 0;
                for (int j = 0; j < eNum; j++) {
                    y[j] = randomP(p);
                    W += y[j];
                }
                if (W < lMin) {
                    continue;
                }
                if (W > lMax) {
                    s += 1;
                    continue;
                }
                NFast++;
                clearSubgraph(subgraph, vNum, MAX);
                for (int j = 0; j < W; j++) {
                    int r = randomEdges(eNum);
                    if (subgraph[edges[0][r]][edges[1][r]] != 1) {
                        subgraph[edges[0][r]][edges[1][r]] = 1;
                        subgraph[edges[1][r]][edges[0][r]] = 1;
                    } else {
                        j--;
                    }
                }
                if (dijkstra(0, vNum, subgraph, MAX) < MAX) {
                    s += 1;
                }
            }
            double pIm = s / N;
            imitationFastModeling.add(pIm);
            if (NFast != 0) {
                advantage.add(N / NFast);
            } else {
                advantage.add(N / (NFast + 1));
            }
        }
        save(imitationFastModeling, "imitationFastModeling.txt");
        save(advantage, "advantage.txt");
        for (int i = 0; i < 11; i ++) {
            System.out.format("%1f%32f%32f", bruteForce.get(i), imitationModeling.get(i), imitationFastModeling.get(i));
            System.out.println();
        }
    }
    public static double pow(double value, int powValue) {
        double result = 1;
        for (int i = 1; i <= powValue; i++) {
            result = result * value;
        }
        return result;
    }
    public static int dijkstra(int start, int vNum, int[][] graph, int MAX) {
        boolean[] used = new boolean[vNum];
        int[] dist = new int[vNum];
        fill(dist, MAX);
        dist[start] = 0;
        for (; ; ) {
            int v = -1;
            for (int nv = 0; nv < vNum; nv++)
                if (!used[nv] && dist[nv] < MAX && (v == -1 || dist[v] >
                        dist[nv]))
                    v = nv;
            if (v == -1) break;
            used[v] = true;
            for (int nv = 0; nv < vNum; nv++)
                if (!used[nv] && graph[v][nv] < MAX)
                    dist[nv] = min(dist[nv], dist[v] + graph[v][nv]);
        }
        return dist[4];
    }
    public static boolean enumeration(int[] check, int edge, int eNum) {
        int j = eNum - 1;
        while (j >= 0 && check[j] == edge) {
            j--;
        }
        if (j < 0) {
            return false;
        }
        if (check[j] >= edge) {
            j--;
        }
        check[j]++;
        if (j == eNum - 1) {
            return true;
        }
        for (int k = j + 1; k < eNum; k++) {
            check[k] = 1;
        }
        return true;
    }
    public static void clearSubgraph(int[][] subgraph, int vNum, int MAX) {
        for (int i = 0; i < vNum; i++) {
            for (int j = 0; j < vNum; j++) {
                if (i == j) {
                    subgraph[i][j] = 1;
                } else {
                    subgraph[i][j] = MAX;
                }
            }
        }
    }
    public static int randomP(double p) {
        return Math.random() < p ? 1 : 0;
    }
    public static int randomEdges(int edges) {
        return (int) (Math.random() * edges);
    }
    public static void save(List<Double> list, String filepath) {
        try {
            FileWriter fileWriter = new FileWriter(filepath);
            for (int i = 0; i < list.size(); ++i) {
                fileWriter.write(i * 0.1 + " " + list.get(i) + "\n");
                fileWriter.flush();
            }
        } catch (IOException exception) {
            exception.printStackTrace();

        }
    }
}