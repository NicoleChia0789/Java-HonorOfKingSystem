/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.honorofkingssystem;


public interface GraphOperations {
    // Vertex operations
    boolean addCharacter(String name);

    boolean removeCharacter(String name);

    // Edge operations
    boolean addRelationship(String c1, String c2, String type);

    boolean removeRelationship(String c1, String c2);

    // Graph display
    void displayGraph();

    // Graph traversal
    void bfsTraversal(String start, int maxLevel);
    
    // New: Complete graph visualization
    void displayFullGraph();
}
