package com.bejker.interactionmanager.client.search;

import com.bejker.interactionmanager.client.InteractionManagerClient;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

import java.util.*;

//TODO: add searching by id
public class SearchUtil {

    private static SearchTree<Block> blockNameTree;


    //Should be called on client init and when language is changed
    public static void init(){
        blockNameTree = new SearchTree<>();

        for(var block : Registries.BLOCK){
            blockNameTree.put(getLocalizedBlockName(block),block);
        }
    }

    public static Collection<Block> searchBlocks(String word,int results){
        return blockNameTree.search(word,results);
    }
    public static String getLocalizedBlockName(Block block){
        return block.getName().getContent().visit(Optional::of).get().toLowerCase(Locale.ROOT);
    }

    private static class SearchTree<T>{
        GeneralizedSuffixTree tree;
        ArrayList<T> list;

        private int idx;
        public SearchTree(){
            tree = new GeneralizedSuffixTree();
            list = new ArrayList<>();
            idx = 0;
        }

        public void put(String word,T entry){
            tree.put(word,idx++);
            list.add(entry);
        }

        public Collection<T> search(String word){
            return search(word,-1);
        }
        public Collection<T> search(String word,int results){
            return tree.search(word,results).stream().map((i) -> list.get(i)).toList();
        }
    }

}
