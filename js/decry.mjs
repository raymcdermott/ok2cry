import { loadFile } from 'nbb';
const { handler } = await loadFile('./src/decry.cljs');

export { handler }