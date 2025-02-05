package com.bejker.interactionmanager.search;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

//TODO: add searching by id
public class SearchUtil {

    private static SearchTree<Block> blockSearchTree;

    private static String current_language;

    //Should be called on client init and when language is changed
    public static void init(){
        String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        if(language.equals(current_language) && blockSearchTree.list.size() == Registries.BLOCK.size()){
            return;
        }
        current_language = language;
        blockSearchTree = new SearchTree<>();

        for(var block : Registries.BLOCK){
            RegistryEntry<Block> entry = Registries.BLOCK.getEntry(block);
            blockSearchTree.put(getLocalizedBlockName(block),block);
            blockSearchTree.put(entry.getIdAsString(),block);
        }
    }

    public static Collection<Block> searchBlocks(String word){
        return searchBlocks(word,-1);
    }
    public static Collection<Block> searchBlocks(String word,int results){
        init();
        return blockSearchTree.search(word,results);
    }
    public static String getLocalizedBlockName(Block block){
        return block.getName().getContent().visit(Optional::of).get().toLowerCase(Locale.ROOT);
    }

    private static class SearchTree<T>{
        GeneralizedSuffixTree tree;
        ArrayList<T> list;

        //Used to map
        HashMap<Integer,Integer> remap;

        private int idx;

        public SearchTree(){
            tree = new GeneralizedSuffixTree();
            list = new ArrayList<>();
            remap = new HashMap<>();
            idx = 0;
        }

        public void put(String word,T entry){
            tree.put(word,idx++);

            int remap_idx = list.indexOf(entry);
            remap.put(idx,list.size());

            if(remap_idx == -1){
                list.add(entry);
            }
        }

        private T mapIndexToEntry(int i){
            return list.get(
                    Objects.requireNonNullElse(remap.get(i), i)
            );
        }

        public Collection<T> search(String word,int results){
            return tree.search(word,results).stream().map(this::mapIndexToEntry).sorted(Comparator.comparing(Object::toString).reversed()).toList();
        }
    }

}
