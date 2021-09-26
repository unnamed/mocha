package team.unnamed.molang.ast;

/**
 * Base interface for representing any node
 * in the abstract syntax tree
 */
public interface Node {

    /**
     * Returns the type of this node instance
     * @see Tokens for all the node types
     */
    default int getNodeType() {
        return 0; // todo
    }

}
