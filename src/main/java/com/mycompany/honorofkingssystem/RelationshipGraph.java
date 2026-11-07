package com.mycompany.honorofkingssystem;

import java.util.*;

// adjacencyList 是一个 LinkedHashMap<String, List<String[]>>
//   - Key   : 人物的名字 (例如 "Liu Bei")
//   - Value : 一个 List，里面装着这个人物的所有邻居关系，每一个关系用 String[]{对方名字, 关系类型} 表示

// 例子：
//   adjacencyList = {
//      "Liu Bei"  -> [ ["Guan Yu", "Sworn Brothers"], ["Zhang Fei", "Sworn Brothers"] ]
//      "Guan Yu"  -> [ ["Liu Bei", "Sworn Brothers"] ]
//      "Zhang Fei"-> [ ["Liu Bei", "Sworn Brothers"] ] }

// 因为是Undirected Graph，所以每一条关系都会同时存在两个人物的。list 里面（A 的 list 里有 B，B 的 list 里也有 A）。
// Traversal  : BFS
// Time O(V+E) | Space O(V)

public class RelationshipGraph implements GraphOperations {

    private LinkedHashMap<String, List<String[]>> adjacencyList;        // Store all the character and relationship

    //用来存放「最近一次」BFS 的结果，给图形窗口 (GraphVisualizer) 使用
    private List<String> lastBfsCharacters = new ArrayList<>();         // BFS 走过的所有人物 (包含起点)，按找到的顺序排列
    private Map<String, Integer> lastBfsLevels = new LinkedHashMap<>(); // 每个人物所在的层数 (起点 = 0)
    private List<String[]> lastBfsEdges = new ArrayList<>();            // BFS 树的边 {上一个人物, 这个人物, 关系类型}

    public RelationshipGraph() {
        adjacencyList = new LinkedHashMap<>();
    }
    

//   这个方法用一个 processed 的 Set 来记录「已经处理过」的关系，
//   key 用 "current|neighbour" 的方式组成，同时也检查反过来的"neighbour|current"，这样同一条边就只会被加入一次。
//      Eg.
//        Liu Bei -> [Guan Yu, Sworn Brothers]
//        Guan Yu -> [Liu Bei, Sworn Brothers]
//     
//        getAllEdges() 只会回传一次：
//         ["Liu Bei", "Guan Yu", "Sworn Brothers"]
//        而不会重复回传 ["Guan Yu", "Liu Bei", "Sworn Brother
    public List<String[]> getAllEdges() {

        List<String[]> edges = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        for (Map.Entry<String, List<String[]>> entry : adjacencyList.entrySet()) {

            String current = entry.getKey();

            for (String[] relation : entry.getValue()) {

                String neighbour = relation[0];
                String type = relation[1];

                String key = current + "|" + neighbour;
                String reverseKey = neighbour + "|" + current;

                if (!processed.contains(key) &&
                    !processed.contains(reverseKey)) {

                    edges.add(new String[]{
                        current,
                        neighbour,
                        type
                    });

                    processed.add(key);
                }
            }
    }

    return edges;
}
    
    // ==================== Display Full Graph Visualization ====================

@Override
public void displayFullGraph() {
    // Get all edges
    List<String[]> allEdges = getAllEdges();
    
    // Get all characters
    Set<String> allCharacters = getAllCharacters();
    
    // Create level map - all characters at level 0 for the visualizer
    Map<String, Integer> levelMap = new LinkedHashMap<>();
    for (String character : allCharacters) {
        levelMap.put(character, 0);
    }
    
    // Show the full graph
    GraphVisualizer.showFullGraph(allCharacters, allEdges, levelMap );
}
    
    
    
    // ==================== Add Character ====================
    /*
     * 流程：
     * 1. 检查这个名字是不是已经存在了，如果存在就不能重复新增。
     * 2. 如果不存在，就在 adjacencyList 里面新增一个 key，
     *    value 是一个空的 List（因为刚新增的人物还没有任何关系）。
     *
     * Eg.
     *   addCharacter("Sun Wukong");
     *   → adjacencyList 多了一笔： "Sun Wukong" -> [] (空的关系列表)
     */
    @Override
    public boolean addCharacter(String name) {
        name = name.trim();

        if (characterExists(name)) {
            System.out.println("  [!] Character already exists.");
            return false;
        }

        adjacencyList.put(name, new ArrayList<>());

        System.out.println("  [OK] Character '" + name + "' added successfully.");

        return true;
    }
    
    //Eg.
    // 已存在 "Liu Bei"
    // characterExists("liubei") → true (视为同一个人)
    public boolean characterExists(String name) {

    String normalizedInput = name.replaceAll("\\s+", "").toLowerCase();

    for (String character : adjacencyList.keySet()) {
        String normalizedExisting = character.replaceAll("\\s+", "").toLowerCase();

        if (normalizedExisting.equals(normalizedInput)) {
            return true;
        }
    }

    return false;
}

    // ==================== Remove Character ====================
    /*
     * 删除一个人物时，不只是把这个人物从 adjacencyList 里面拿掉，
     * 还要把「所有指向这个人物的关系」也一起清掉，不然其他人物的
     * list 里面会留下一个已经不存在的名字，之后搜寻/遍历就会出错。
     *
     * Time Complexity: O(V + E)
     *
     * Eg.
     *   Zhuang Zhou → [Yu, Friend]
     *   Yu          → [Zhuang Zhou, Friend]
     *               → [Pang Xie, Friend]
     *   Fa Meng     → [Zhuang Zhou, Rival]
     *
     *   removeCharacter("Zhuang Zhou");
     *
     *   1. For: 一个一个拿出每个 Character 的 relationship list
     *           第一次可能是 Zhuang Zhou 的 list，第二次是 Yu 的 list...
     *   2. For: 检查当前 Character 的每一个 relationship
     *   3. If : 如果 neighbours.get(i)[0] 等于 "Zhuang Zhou"
     *           就把这个 relationship 删掉
     *
     *   Result:
     *   Yu      → [Pang Xie, Friend]
     *   Fa Meng → []
     */

    @Override
    public boolean removeCharacter(String name) {
        String actualName = findCharacterName(name);

        if (actualName == null) {
            System.out.println("  [!] Character not found.");
            return false;
        }

        // 检查所有人物，把指向 "actualName" 这个人物的关系删除
        for (List<String[]> neighbours : adjacencyList.values()) {      // 检查每一个 Character
            for (int i = neighbours.size() - 1; i >= 0; i--) {          // 检查这个 Character 的每一个 relationship
                if (neighbours.get(i)[0].equals(actualName)) {
                    neighbours.remove(i);   
                }
            }
        }

        adjacencyList.remove(actualName);

        System.out.println("  [OK] Character '" + actualName + "' and all relationships removed.");

        return true;
    }

    // ==================== Add Relationship ====================
    /*
     * 流程：
     * 1. 检查 c1、c2 这两个人物是不是都存在。
     * 2. 检查是不是同一个人 (不能跟自己有关系)。
     * 3. 检查这两人之间是不是已经有关系了 (避免重复新增)。
     * 4. 因为是无向图，所以要「双向」新增：
     *      c1 的 list 里加入 c2
     *      c2 的 list 里加入 c1
     *
     * Eg.
     *   addRelationship("Liu Bei", "Zhuge Liang", "Lord & Strategist");
     *   → Liu Bei    的 list 多了 ["Zhuge Liang", "Lord & Strategist"]
     *   → Zhuge Liang 的 list 多了 ["Liu Bei", "Lord & Strategist"]
     */
    @Override
    public boolean addRelationship(String c1, String c2, String type) {

        String actualC1 = findCharacterName(c1);
        String actualC2 = findCharacterName(c2);

        if (actualC1 == null) {
            System.out.println("  [!] First character not found.");
            return false;
        }

        if (actualC2 == null) {
            System.out.println("  [!] Second character not found.");
            return false;
        }

        if (actualC1.equals(actualC2)) {
            System.out.println("  [!] A character cannot have a relationship with itself.");
            return false;
        }

        adjacencyList.get(actualC1).add(new String[]{actualC2, type});

        adjacencyList.get(actualC2).add(new String[]{actualC1, type});

        System.out.println("  [OK] Added: " + actualC1 + " <--[ " + type + " ]--> " + actualC2);

        return true;
    }
    
    //Check if relationship exists
    public boolean relationshipExists(String character1, String character2) {
        List<String[]> relationships = adjacencyList.get(character1);

        if (relationships != null) {
            for (String[] relationship : relationships) {
                if (relationship[0].equals(character2)) {
                    return true;
                }
            }
        }

        return false;
    }

    // ==================== Remove Relationship ====================
    /*
     * 跟 addRelationship 相反，一样要「双向」删除：
     *   从 c1 的 list 里面，删掉指向 c2 的那一条关系
     *   从 c2 的 list 里面，删掉指向 c1 的那一条关系
     *
     * Eg.
     *   Liu Bei    → [Guan Yu, Sworn Brothers]
     *   Guan Yu    → [Liu Bei, Sworn Brothers]
     *
     *   removeRelationship("Liu Bei", "Guan Yu");
     *
     *   1. 在 Liu Bei 的 list 里面找到 "Guan Yu"，删除
     *   2. 在 Guan Yu 的 list 里面找到 "Liu Bei"，删除
     *
     *   Result:
     *   Liu Bei → []
     *   Guan Yu → []
     */
    @Override
    public boolean removeRelationship(String c1, String c2) {

        String actualC1 = findCharacterName(c1);
        String actualC2 = findCharacterName(c2);

        if (actualC1 == null || actualC2 == null) {
            System.out.println("  [!] One or both characters not found.");
            return false;
        }

        boolean removed1 = removeNeighbour(actualC1, actualC2);     // 从 c1 的 relationship list 里面，找出并删除指向 c2 的那一条
        boolean removed2 = removeNeighbour(actualC2, actualC1);     // 从 c2 的 relationship list 里面，找出并删除指向 c1 的那一条

        if (removed1 && removed2) {
            System.out.println("  [OK] Relationship removed.");
            return true;
        }

        System.out.println("  [!] No relationship found.");
        return false;
    }

    
    
    private boolean removeNeighbour(String character,  String neighbour) {

        List<String[]> neighbours = adjacencyList.get(character);

        for (int i = 0; i < neighbours.size(); i++) {
            if (neighbours.get(i)[0].equals(neighbour)) {
                neighbours.remove(i);
                return true;
            }
        }

        return false;
    }

    // ==================== Search Character ====================
    /*
     * Eg.
     *   searchCharacter("Liu Bei");
     *   → 印出 Liu Bei 的所有 neighbours，例如：
     *     Guan Yu    | Sworn Brothers
     *     Zhang Fei  | Sworn Brothers
     *     Zhuge Liang| Lord & Strategist
     */
    public void searchCharacter(String name) {
        String actualName = findCharacterName(name);

        if (actualName == null) {
            System.out.println("  [!] Character not found.");
            return;
        }

        List<String[]> neighbours = adjacencyList.get(actualName);

        System.out.println();
        ConsoleUI.printHeader("CHARACTER PROFILE");
        System.out.printf("  %-14s: %s%n", "Character", actualName);
        System.out.printf("  %-14s: %d%n", "Connections", neighbours.size());
        ConsoleUI.printLightLine();

        if (neighbours.isEmpty()) {
            System.out.println("  No relationships.");
        } else {
            System.out.printf("  %-25s %s%n", "Connected To", "Relationship");
            ConsoleUI.printLightLine();

            for (String[] relation : neighbours) {
                System.out.printf("  %-25s %s%n", relation[0], relation[1]);
            }
        }

        ConsoleUI.printFooter();
    }

    // ==================== Display Graph ====================
    // 把 adjacencyList 里的每一个character，以及他们的relationship全部list出来。
    // 因为undirectgraph的每条关系会被存两次 (A→B 和 B→A)，所以计算「关系总数」的时候要除以 2。

    @Override
    public void displayGraph() {
        System.out.println();
        ConsoleUI.printHeader("HONOR OF KINGS - COMPLETE RELATIONSHIP NETWORK");

        System.out.printf("  %-15s: %d%n", "Characters", adjacencyList.size());
        System.out.printf("  %-15s: %d%n", "Relationships", getRelationshipCount());

        ConsoleUI.printLightLine();

        for (Map.Entry<String, List<String[]>> entry : adjacencyList.entrySet()) {

            String character = entry.getKey();
            List<String[]> neighbours = entry.getValue();

            System.out.println();
            System.out.println("  " + character + ":");

            if (neighbours.isEmpty()) {
                System.out.println("      (none)");
            } else {
                for (String[] relation : neighbours) {
                    System.out.printf("      -> %-20s [%s]%n", relation[0], relation[1]);
                }
            }
        }

        System.out.println();
        ConsoleUI.printFooter();
    }

    // ==================== BFS ====================
    /*
     * BFS 用一个 Queue 来实现，概念是「先找到的先扩展」，
     * 所以会一层一层地往外扩散，像丢一颗石头到水里产生的水波一样。
     *
     * 流程：
     * 1. 把起点放进 Queue，标记为已拜访 (visited)，层数 (level) = 0
     * 2. 不断从 Queue 拿出一个人物 cur：
     *      - 如果 cur 的层数已经到达 maxLevel，就不再往下扩展
     *      - 否则，检查 cur 的每一个邻居：
     *          如果还没拜访过，就标记拜访、放进 Queue、层数 = cur 的层数 + 1
     * 3. 重复直到 Queue 空了为止
     *
     * Eg. (maxLevel = 2, start = "Li Bai")
     *   Level 0: Li Bai
     *   Level 1: Han Xin, Zhao Yun          (Li Bai 直接认识的人)
     *   Level 2: Sun Shangxiang, Zhuge Liang (透过 Level 1 认识的人)
     *
     *   lastBfsEdges 会记录：
     *     [Li Bai, Han Xin, Teammates]
     *     [Li Bai, Zhao Yun, Teammates]
     *     [Han Xin, Sun Shangxiang, Teammates]
     *     [Zhao Yun, Zhuge Liang, Teammates]
     */
    @Override
    public void bfsTraversal(
            String start,
            int maxLevel) {

        String actualStart = findCharacterName(start);

        if (actualStart == null) {
            System.out.println("  [!] Character not found.");
            return;
        }

        // 每次开始新的 BFS，先把上一次的记录清空
        lastBfsCharacters.clear();
        lastBfsLevels.clear();
        lastBfsEdges.clear();

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new LinkedHashSet<>();

        queue.add(actualStart);
        visited.add(actualStart);

        lastBfsLevels.put(actualStart, 0);
        lastBfsCharacters.add(actualStart);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentLevel = lastBfsLevels.get(current);

            if (currentLevel >= maxLevel) {
                continue;
            }

            List<String[]> neighbours = adjacencyList.get(current);

            for (String[] relation : neighbours) {
                String neighbour = relation[0];
                String relationshipType = relation[1];

                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);

                    int newLevel = currentLevel + 1;

                    lastBfsLevels.put(neighbour, newLevel);
                    lastBfsCharacters.add(neighbour);

                    lastBfsEdges.add(new String[]{ current, neighbour, relationshipType });
                }
            }
        }

        displayBfsResult(actualStart, maxLevel);
    }

    // ==================== Display BFS Result ====================

    private void displayBfsResult(String start, int maxLevel) {

        System.out.println();
        ConsoleUI.printHeader("BFS TRAVERSAL RESULT");
        System.out.printf("  %-20s: %s%n", "Starting Character", start);
        System.out.printf("  %-20s: %d%n", "Maximum Level", maxLevel);
        ConsoleUI.printLightLine();

        System.out.println();
        System.out.println("  Traversal Order:");

        // Display traversal order with line wrapping (6 names per line)
        System.out.print("    ");

        int namesPerLine = 6;

        for (int i = 0; i < lastBfsCharacters.size(); i++) {
            System.out.print(lastBfsCharacters.get(i));

            if (i < lastBfsCharacters.size() - 1) {
                System.out.print(" -> ");
            }
            if ((i + 1) % namesPerLine == 0 && i < lastBfsCharacters.size() - 1) {
                System.out.println();
                System.out.print("    ");
            }
        }
        System.out.println();

        System.out.println();
        ConsoleUI.printLightLine();

        int actualMaxLevel = 0;

        for (int level : lastBfsLevels.values()) {
            if (level > actualMaxLevel) {
                actualMaxLevel = level;
            }
        }

        for (int level = 0; level <= actualMaxLevel; level++) {

            System.out.println();
            System.out.println("  Level " + level + ":");

            if (level == 0) {
                System.out.println("    " + start);
                continue;
            }

            for (String[] edge : lastBfsEdges) {
                String parent = edge[0];
                String child = edge[1];
                String relationship = edge[2];

                if (lastBfsLevels.get(child) == level) {
                    System.out.printf("    %s -> %s   [%s]%n", parent, child, relationship);
                }
            }
        }

        System.out.println();
        ConsoleUI.printFooter();
    }

    // ==================== Find Character ====================

    private String findCharacterName(String name) {
        if (name == null) {
            return null;
        }

        for (String character : adjacencyList.keySet()) {
            if (character.equalsIgnoreCase(name.trim())) {
                return character;
            }
        }

        return null;
    }

    // ==================== Get Character By Number ====================

    public String getCharacterByNumber(int number) {
        if (number < 1 || number > adjacencyList.size()) {
            return null;
        }

        int currentNumber = 1;

        for (String name : adjacencyList.keySet()) {
            if (currentNumber == number) {
                return name;
            }

            currentNumber++;
        }

        return null;
    }

    // ==================== Get All Characters ====================

    public Set<String> getAllCharacters() {
        return adjacencyList.keySet();
    }

    // ==================== Character Count ====================

    public int getCharacterCount() {
        return adjacencyList.size();
    }

    // ==================== Relationship Count ====================

    public int getRelationshipCount() {
        int total = 0;

        for (List<String[]> neighbours : adjacencyList.values()) {
            total += neighbours.size();
        }

        // Each undirected relationship is stored twice.
        return total / 2;
    }

    // ==================== BFS Getters ====================

    public List<String> getLastBfsCharacters() {
        return lastBfsCharacters;
    }

    public Map<String, Integer> getLastBfsLevels() {
        return lastBfsLevels;
    }

    public List<String[]> getLastBfsEdges() {
        return lastBfsEdges;
    }

    // ==================== Initial Data ====================

    public void loadInitialData() {
        String[] characters = {
            "Han Xin",
            "Liu Bei",
            "Zhao Yun",
            "Diao Chan",
            "Lu Bu",
            "Li Bai",
            "Sun Ce",
            "Da Qiao",
            "Xiao Qiao",
            "Zhou Yu",
            "Zhuge Liang",
            "Cao Cao",
            "Guan Yu",
            "Zhang Fei",
            "Sun Shangxiang"
        };

        for (String character : characters) {
            adjacencyList.put(
                    character,
                    new ArrayList<>()
            );
        }

        addInitialRelationship(
                "Lu Bu",
                "Diao Chan",
                "Lovers"
        );

        addInitialRelationship(
                "Sun Ce",
                "Da Qiao",
                "Married Couple"
        );

        addInitialRelationship(
                "Zhou Yu",
                "Xiao Qiao",
                "Lovers"
        );

        addInitialRelationship(
                "Sun Ce",
                "Sun Shangxiang",
                "Siblings"
        );

        addInitialRelationship(
                "Da Qiao",
                "Xiao Qiao",
                "Sisters"
        );

        addInitialRelationship(
                "Liu Bei",
                "Guan Yu",
                "Sworn Brothers"
        );

        addInitialRelationship(
                "Liu Bei",
                "Zhang Fei",
                "Sworn Brothers"
        );

        addInitialRelationship(
                "Guan Yu",
                "Zhang Fei",
                "Sworn Brothers"
        );

        addInitialRelationship(
                "Li Bai",
                "Han Xin",
                "Teammates"
        );

        addInitialRelationship(
                "Li Bai",
                "Zhao Yun",
                "Teammates"
        );

        addInitialRelationship(
                "Han Xin",
                "Sun Shangxiang",
                "Teammates"
        );

        addInitialRelationship(
                "Zhao Yun",
                "Zhuge Liang",
                "Teammates"
        );

        addInitialRelationship(
                "Liu Bei",
                "Zhuge Liang",
                "Lord & Strategist"
        );

        addInitialRelationship(
                "Cao Cao",
                "Zhuge Liang",
                "Lord & Strategist"
        );

        addInitialRelationship(
                "Lu Bu",
                "Cao Cao",
                "Rivals"
        );

        addInitialRelationship(
                "Liu Bei",
                "Cao Cao",
                "Rivals"
        );

        addInitialRelationship(
                "Li Bai",
                "Lu Bu",
                "Rivals"
        );
    }

    // ==================== Initial Relationship ====================

    private void addInitialRelationship(
            String a,
            String b,
            String type) {

        adjacencyList.get(a).add(
                new String[]{b, type}
        );

        adjacencyList.get(b).add(
                new String[]{a, type}
        );
    }
}