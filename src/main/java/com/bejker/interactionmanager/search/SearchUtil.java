package com.bejker.interactionmanager.search;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;

//TODO: add searching by id
public class SearchUtil {

    private static SearchTree<Block> blockSearchTree;

    private static SearchTree<EntityType<?>> entitySearchTree;

    private static SearchTree<Item> itemSearchTree;

    private static String current_language;

    //Should be called on client init and when language is changed
    public static void init(){
        String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        if(language.equals(current_language) && blockSearchTree.list.size() == Registries.BLOCK.size()){
            return;
        }
        current_language = language;
        blockSearchTree = new SearchTree<>();
        entitySearchTree = new SearchTree<>();
        itemSearchTree = new SearchTree<>();

        for(var block : Registries.BLOCK){
            RegistryEntry<Block> entry = Registries.BLOCK.getEntry(block);
            blockSearchTree.put(getLocalizedName(block.getName()),block);
            blockSearchTree.put(entry.getIdAsString(),block);
        }

        for(var entity_type : Registries.ENTITY_TYPE){
            RegistryEntry<EntityType<?>> entry = Registries.ENTITY_TYPE.getEntry(entity_type);
            entitySearchTree.put(getLocalizedName(entity_type.getName()),entity_type);
            entitySearchTree.put(entry.getIdAsString(),entity_type);
        }

        for(var item : Registries.ITEM){
            RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);
            itemSearchTree.put(getLocalizedName(item.getName()),item);
            itemSearchTree.put(entry.getIdAsString(),item);
        }
    }


    public static Collection<Block> searchBlocks(String word,int results,Predicate<? super Block> filterPredicate){
        init();
        return blockSearchTree.search(word,results).stream()
                .distinct()
                .filter(filterPredicate)
                .sorted(Comparator.comparingInt(x -> modLSD(
                        getLocalizedName(x.getName()),
                        word,0)))
                .toList();
    }

    public static Collection<EntityType<?>> searchEntities(String word,int results,Predicate<? super EntityType<?>> filterPredicate){
        init();
        return entitySearchTree.search(word,results).stream()
                .distinct()
                .filter(filterPredicate)
                .sorted(Comparator.comparingInt(x -> modLSD(
                        getLocalizedName(x.getName()),
                        word,0)))
                .toList();
    }

    public static String getLocalizedName(Text text){
        return text.getContent().visit(Optional::of).get().toLowerCase(Locale.ROOT);
    }

    public static Collection<Item> searchItems(String word, int results, Predicate<? super Item> filterPredicate) {
        init();
        return itemSearchTree.search(word,results).stream()
                .distinct()
                .filter(filterPredicate)
                .sorted(Comparator.comparingInt(x -> modLSD(
                        getLocalizedName(x.getName()),
                        word,0)))
                .toList();
    }

    private static final HashMap<Pair<String,String>, Integer> lsdCache = new HashMap<>();

    // Modified Leven Shtein Distance, added cutoff to reduce computation
    private static int modLSD(String search, String word, int curr){
        final int CUTOFF = 3;
        if(curr >= CUTOFF){
            return Math.max(search.length(),word.length());
        }
        Pair<String,String> pair = new Pair<>(search,word);
        Integer ret = lsdCache.get(pair);
        if(ret != null){
            return ret;
        }
        // Don't cache trivial cases
       if(search.isBlank()){
           return word.length();
       }
       if(word.isBlank()){
            return search.length();
       }
       String searchTail = search.substring(1);
       String wordTail = word.substring(1);
       if(search.charAt(0) == word.charAt(0)){
           ret = modLSD(searchTail,wordTail,curr + 1);
           lsdCache.put(pair,ret);
           return ret;
       }
       ret = 1 +  Math.min(
               Math.min(modLSD(searchTail,word, curr + 1), modLSD(search,wordTail, curr + 1)),
               modLSD(searchTail,wordTail, curr + 1)
       );
       lsdCache.put(pair,ret);
       return ret;
    }

    //public static Collection<Item> searchItems(String word) {
    //    return searchItems(word,-1);
    //}

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
            return tree.search(word,results).stream().map(this::mapIndexToEntry).toList();
        }
    }

}
