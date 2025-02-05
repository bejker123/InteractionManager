package com.bejker.interactionmanager.search;

import com.bejker.interactionmanager.InteractionManager;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;

import java.util.*;

//TODO: add searching by id
public class SearchUtil {

    private static SearchTree<Block> blockNameTree;

    private static String current_language;

    //Should be called on client init and when language is changed
    public static void init(){
        String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        if(language.equals(current_language) && blockNameTree.list.size() == Registries.BLOCK.size()){
            return;
        }
        current_language = language;
        blockNameTree = new SearchTree<>();

        for(var block : Registries.BLOCK){
            blockNameTree.put(getLocalizedBlockName(block),block);
        }
    }

    public static Collection<Block> searchBlocks(String word,int results){
        init();
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
