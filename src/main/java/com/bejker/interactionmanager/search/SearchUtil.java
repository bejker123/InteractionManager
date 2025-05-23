package com.bejker.interactionmanager.search;

import com.bejker.interactionmanager.InteractionManager;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

//TODO: add searching by id
public class SearchUtil {

    /** Only applies to translated names, not to IDs
     * Used in {@link SearchUtil#getLocalizedName(Text)}
     */
    private static final int MAX_SEARCH_TERM_LEN = 40;

    private static final SearchTree<Block> blockSearchTree = new SearchTree<>();

    private static final SearchTree<EntityType<?>> entitySearchTree = new SearchTree<>();

    private static final SearchTree<Item> itemSearchTree = new SearchTree<>();

    private static final ImmutableSet<String> allSearchTreeNames = ImmutableSet.of("block","entity","item");

    private static String currentLanguage;
    private static ImmutableSet<String> currentResourcePacks;

    public static void init(){
        SearchUtil.init(false);
    }

    /** Should be called on client init and when language or resource pack is changed
     * @param force whether to force reload, even if language, resource pack, and the registry hasn't changed
     * @see com.bejker.interactionmanager.IMReloadListener#onReload()
     */
    public static void init(boolean force){
        String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        ImmutableSet<String> resourcePacks = (ImmutableSet<String>) MinecraftClient.getInstance().getResourcePackManager().getEnabledIds();

        // Don't update the search trees if we don't register any changes
        if(!force &&
                language.equals(currentLanguage) &&
                blockSearchTree.list.size() == Registries.BLOCK.size() &&
                entitySearchTree.list.size() == Registries.ENTITY_TYPE.size() &&
                itemSearchTree.list.size() == Registries.ITEM.size() &&
                Objects.equals(resourcePacks,currentResourcePacks)){
            return;
        }
        currentLanguage = language;
        currentResourcePacks = resourcePacks;

        blockSearchTree.clear();
        entitySearchTree.clear();
        itemSearchTree.clear();

        HashSet<String> builtSearchTrees = new HashSet<>();
        StringWriter errorStringWriter = new StringWriter();

        try {
            SearchUtil.internalInit(builtSearchTrees);
        }catch (Exception e){
            PrintWriter printWriter = new PrintWriter(errorStringWriter);
            e.printStackTrace(printWriter);
        }

        if(!errorStringWriter.toString().isBlank()){
            StringBuilder failedToBuildS = new StringBuilder();
            List<String> failedToBuild = allSearchTreeNames.stream().filter((x) -> !builtSearchTrees.contains(x)).toList();
            for(String i : failedToBuild){
                failedToBuildS.append(i).append(", ");
            }
            InteractionManager.LOGGER.error("Failed to build search tree{}: {}with error:\n{}",
                    failedToBuild.size() == 1 ? "" : "s",
                    failedToBuildS,
                    errorStringWriter);
            return;
        }
        InteractionManager.LOGGER.info("Built search trees");
    }

    private static void internalInit(HashSet<String> builtSearchTrees){
        for(var block : Registries.BLOCK){
            RegistryEntry<Block> entry = Registries.BLOCK.getEntry(block);
            blockSearchTree.put(getNamespace(entry),getLocalizedName(block.getName()),block);

            //blockSearchTree.put(entry.getIdAsString(),block);
        }
        builtSearchTrees.add("block");

        for(var entity_type : Registries.ENTITY_TYPE){
            RegistryEntry<EntityType<?>> entry = Registries.ENTITY_TYPE.getEntry(entity_type);
            entitySearchTree.put(getNamespace(entry),getLocalizedName(entity_type.getName()),entity_type);

            //entitySearchTree.put(entry.getIdAsString(),entity_type);
        }
        builtSearchTrees.add("entity");

        for(var item : Registries.ITEM){
            RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);
            itemSearchTree.put(getNamespace(entry),getLocalizedName(item.getName()),item);

            //itemSearchTree.put(entry.getIdAsString(),item);
        }
        builtSearchTrees.add("item");
    }

    private static String getNamespace(RegistryEntry<?> entry){
        String idString = entry.getIdAsString();
        int idx = idString.lastIndexOf(':');
        if(idx == -1){
            InteractionManager.LOGGER.warn("{} doesn't have a namespace!",entry.getKey());
            return "";
        }
        String namespaceString = idString.substring(0,idx);
        //InteractionManager.LOGGER.info("{}, {}",namespaceString,entry.getKey());
        return namespaceString.toLowerCase(Locale.ROOT);
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
        String content = text.getContent().visit(Optional::of).get();
        // Clamp the translated string size, to prevent both long search times and huge utf-16 strings
        return content.substring(0,Math.min(content.length(), MAX_SEARCH_TERM_LEN))
                .toLowerCase(Locale.ROOT);
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


    private static class SearchTree<T>{
        GeneralizedSuffixTree tree;
        final ArrayList<T> list = new ArrayList<>();
        final ArrayList<String> namespaceList = new ArrayList<>();

        private int idx;

        public SearchTree(){
            this.clear();
        }

        public void clear(){
            tree = new GeneralizedSuffixTree();
            list.clear();
            namespaceList.clear();
            idx = 0;
        }

        public void put(String namespace,String word,final T entry){
            word = word.toLowerCase(Locale.ROOT);
            try {
                tree.put(word,idx);
            }catch (NullPointerException e){
                StringWriter stringWriter = new StringWriter();
                PrintWriter writer = new PrintWriter(stringWriter);
                e.printStackTrace(writer);
                InteractionManager.LOGGER.error(
                        "Failed to put word in a search tree. It's likely because it contains non-UTF-8 characters, culprit word: '{}', error:\n{}",
                        word,
                        stringWriter
                );
            }
            list.add(entry);
            namespaceList.add(namespace);
            // Increment the index last to prevent accidental use of the next index
            idx++;
        }

        private T mapIndexToEntry(int i){
            return list.get(i);
            //return list.get(
            //        Objects.requireNonNullElse(remap.get(i), i)
            //);
        }

        private Stream<Integer> rawSearch(String word,int results){
           return tree.search(word.toLowerCase(Locale.ROOT),results).stream();
        }

        public Collection<T> search(String word,int results){
            word = word.toLowerCase(Locale.ROOT);

            int startNamespaceIdx = word.indexOf('@') + 1;
            if(startNamespaceIdx != 0&&startNamespaceIdx < word.length()){
               int endNamespaceIdx = word.substring(startNamespaceIdx,word.length() - 1).indexOf(" ");
               if(endNamespaceIdx == -1){
                   endNamespaceIdx = word.length();
               }else{
                   endNamespaceIdx += startNamespaceIdx;
               }
               String namespace = word.substring(startNamespaceIdx,endNamespaceIdx);
               word = (word.substring(0,startNamespaceIdx - 1) + word.substring(endNamespaceIdx)).trim();
               return search(namespace,word,results);
            }
            if(word.length() > 1 && startNamespaceIdx == word.length()){
                word = word.substring(0,word.length() - 2).trim();
            }
            return rawSearch(word,results).map(this::mapIndexToEntry).toList();
        }

        public Collection<T> search(String namespace,String word,int results){
            return rawSearch(word, results)
                    .filter((idx)->this.filterByNamespace(namespace,idx))
                    .map(this::mapIndexToEntry).toList();
        }

        private boolean filterByNamespace(String namespace, Integer idx) {
            if(idx == null) {
                return false;
            }
            String foundNamespace = namespaceList.get(idx);
            if(foundNamespace == null){
                return false;
            }
            int matchIdx = foundNamespace.indexOf(namespace);
            // Change to >= to allow partial matches
            return matchIdx == 0;
        }
    }

}
