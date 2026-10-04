package tech.javelin.base.filemanager.impl;

import com.google.common.reflect.TypeToken;
import java.util.HashSet;
import java.util.Set;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.filemanager.api.ManagerFileAbstract;

public class FriendManager extends ManagerFileAbstract<String> {
   public FriendManager() {
      super("friends.json", "", (new TypeToken<Set<String>>() {
      }).getType(), HashSet::new);
   }

   public boolean isFriend(String name) {
      return this.getItems().contains(name);
   }

   @Native
   public boolean removeFriend(String name) {
      return this.getItems().remove(name);
   }
}
