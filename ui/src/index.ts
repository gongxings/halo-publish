import { definePlugin } from "@halo-dev/console-shared";
import type { ListedPost } from '@halo-dev/api-client';
import type { Ref } from 'vue';


export default definePlugin({
  components: {},
  routes: [
  ],
  extensionPoints: {
    'post:list-item:operation:create': (post: Ref<ListedPost>) => {
      return []
    }
  },
});
