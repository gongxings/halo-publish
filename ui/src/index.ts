import { definePlugin } from "@halo-dev/console-shared";
import { consoleApiClient, type ListedPost } from '@halo-dev/api-client';


export default definePlugin({
  components: {},
  routes: [
  ],
  extensionPoints: {
    'post:list-item:operation:create': (post: ListedPost) => {
      return []
    }
  },
});
